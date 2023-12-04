package DataAcessObject;

import Entidades.*;
import Conexao.Conexao;
import Extrator.ExtrairDouble;
import com.github.britooo.looca.api.util.Conversor;
import com.mysql.cj.protocol.a.LocalDateTimeValueEncoder;
import org.checkerframework.checker.units.qual.C;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static DataAcessObject.AlertaDAO.cadastrarAlerta;

public class StatusPcDAO {
    Integer idCaptura = 0;
    StatusPc statusPc = new StatusPc();
    static PreparedStatement ps = null;
    static ResultSet rs = null;
    static PreparedStatement psSQLServer = null;
    static ResultSet rsSQLServer = null;
    public static void exibirInformacoesMaquina(String nomeProcessador, String sistemaOperacional,
                                                Long memoriaTotal,Long discoTotal, Integer qtdDiscos){

        System.out.println(String.format("""
             +==============================================================================+
             ||                         Informações da máquina                             ||
             +==============================================================================+
                                                                                         
                Processador: %s                                                          
                Sistema Operacional: %s                                                  
                Memória total: %s                                                        
                Disco total: %s 
                Quantidade de Discos: %s                                                         
                                                                                         
             +==============================================================================+
                """, nomeProcessador, sistemaOperacional,
                Conversor.formatarBytes(memoriaTotal), Conversor.formatarBytes(discoTotal), qtdDiscos));

    }
    public static String pegarIdCaptura (StatusPc statusPc){
        String sql = "SELECT idCaptura FROM status_pc";
        PreparedStatement ps = null;
        ResultSet rs = null; // ResultSet é uma classe utilizada para poder realizar os selects
        try{
            psSQLServer = Conexao.getConexao().prepareStatement(sql);
            rsSQLServer = psSQLServer.executeQuery();
            while(rsSQLServer.next()) { // o  next é para ele mover para a prox. linha
                statusPc.setIdCaptura(rsSQLServer.getInt(1));
            }
            ps = Conexao.getConexao().prepareStatement(sql);
            rs = ps.executeQuery();
            while(rs.next()) { // o  next é para ele mover para a prox. linha
                statusPc.setIdCaptura(rs.getInt(1));
            }
            ps.execute();
            psSQLServer.execute();
        } catch (SQLException e ){
            e.printStackTrace();
        }
        return sql;
    }
    public static void cadastrarCapturas( StatusPc statusMemoria, StatusPc statusProcessador, StatusPc Disco,
                                          StatusPc dtHora, Computador computador) {
        String sql = "INSERT INTO status_pc " +
                "(memoriaUso, processadorUso, discoDisponivel, dtHoraCaptura, fkComputador) " +
                "VALUES (?, ?, ?, ?, ?)";

        String sqlServer = "INSERT INTO status_pc " +
                "(memoriaUso, processadorUso, discoDisponivel, dtHoraCaptura, fkComputador) " +
                "VALUES (?, ?, ?, GETDATE(), ?)";

        PreparedStatement ps = null;
        LocalDateTime dtHoraAtual = LocalDateTime.now();

        try {
            ps = Conexao.getConexao().prepareStatement(sql);
            Double memoriaUso = ExtrairDouble.extrairNumero(Conversor.formatarBytes(statusMemoria.getMemoriaUso()));
            if (memoriaUso > 64){
                memoriaUso /= 1024;
            }
            ps.setDouble(1, memoriaUso);
            ps.setDouble(2, statusProcessador.getProcessadorEmUso());
            ps.setDouble(3, ExtrairDouble.extrairNumero(Conversor.formatarBytes(Disco.getDiscoDisponivel())));
            ps.setString(4, String.valueOf(dtHoraAtual));
            ps.setString(5, computador.getId());
            ps.execute();

            psSQLServer = Conexao.getConexaoSQLServer().prepareStatement(sqlServer);
            psSQLServer.setDouble(1, memoriaUso);
            psSQLServer.setDouble(2, statusProcessador.getProcessadorEmUso());
            psSQLServer.setDouble(3,  ExtrairDouble.extrairNumero(Conversor.formatarBytes(Disco.getDiscoDisponivel())));
            psSQLServer.setString(4, computador.getId());
            psSQLServer.execute();

            String dataFormatadaa = String.valueOf(dtHora.getDtHoraCaptura());
            Date dataAtual = new Date();
            // Definir o formato desejado
            SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            // Formatar a data
            String dataFormatada = formato.format(dataAtual);

            System.out.printf(String.format("""
                +================================================+
                ||               Dados da captura               ||
                +================================================+
                                                                                                                  
                              cpu em uso: %.2f                
                            Memórria em uso: %s               
                           Disco Disponivel: %s
                           data/hora da captura: %s            
                                                              
                +================================================+      
                
                """,
                    statusProcessador.getProcessadorEmUso(),
                    Conversor.formatarBytes(statusMemoria.getMemoriaUso()),
                    Conversor.formatarBytes(Disco.getDiscoDisponivel()),
                    dataFormatada));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void verificarEAlertar(Computador computador, Double limiteAlerta) {
        String sql = "SELECT AVG(processadorUso) AS mediaProcessador FROM status_pc WHERE fkComputador = ? AND dtHoraCaptura >= NOW() - INTERVAL 1 MINUTE";

        String sqlServer = "SELECT AVG(processadorUso) AS mediaProcessador FROM status_pc WHERE fkComputador = ? AND dtHoraCaptura >= DATEADD(MINUTE, -1, GETDATE())";

        PreparedStatement ps = null;
        ResultSet rs = null;

        PreparedStatement psSQLServer = null;
        ResultSet rsSQLServer = null;

        try {
            ps = Conexao.getConexao().prepareStatement(sql);
            ps.setString(1, computador.getId());

            rs = ps.executeQuery();

            if (rs.next()) {
                String mediaProcessadorStr = rs.getString("mediaProcessador");

                // Verifica se a média é maior que o limite
                if (mediaProcessadorStr != null) {
                    mediaProcessadorStr = mediaProcessadorStr.replace("%", "").trim(); // Remover o símbolo de percentagem e espaços extras
                    Double mediaProcessador = Double.parseDouble(mediaProcessadorStr);

                    // Usar um valor epsilon para lidar com a precisão dos números de ponto flutuante
                    double epsilon = 0.001;  // Ajuste conforme necessário
                    if (mediaProcessador > limiteAlerta + epsilon) {
                        Alerta alertaProcessador = new Alerta();
                        alertaProcessador.setDescricao("Uso elevado de processador. Média de uso: %.2f%%".formatted(mediaProcessador));
                        alertaProcessador.setDtHoraAlerta(LocalDateTime.now().toString());
                        // Adicione outras informações ao alerta, se necessário
                    }
                } else {
                    System.out.println("A coluna 'mediaProcessador' está com valor nulo.");
                }
            }

            psSQLServer = Conexao.getConexaoSQLServer().prepareStatement(sqlServer);
            psSQLServer.setString(1, computador.getId());

            rsSQLServer = psSQLServer.executeQuery();

            if (rsSQLServer.next()) {
                String mediaProcessadorStr = rsSQLServer.getString("mediaProcessador");

                // Verificar se mediaMemoriaStr não é nulo
                if (mediaProcessadorStr != null) {
                    mediaProcessadorStr = mediaProcessadorStr.replace("%", "").trim(); // Remover o símbolo de percentagem e espaços extras
                    Double mediaProcessador = Double.parseDouble(mediaProcessadorStr);

                    // Usar um valor epsilon para lidar com a precisão dos números de ponto flutuante
                    double epsilon = 0.001;  // Ajuste conforme necessário
                    if (mediaProcessador > limiteAlerta + epsilon) {
                        Alerta alertaProcessador = new Alerta();
                        alertaProcessador.setDescricao("Uso elevado de processador. Média de uso: %.2f%%".formatted(mediaProcessador));
                        alertaProcessador.setDtHoraAlerta(LocalDateTime.now().toString());
                        // Adicione outras informações ao alerta, se necessário
                        cadastrarAlerta(alertaProcessador, computador, "Processador");
                    }
                } else {
                    System.out.println("A coluna 'mediaProcessador' está com valor nulo.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // Fechar recursos (ResultSet, PreparedStatement, etc.)
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();

                if (rsSQLServer != null) rsSQLServer.close();
                if (psSQLServer != null) psSQLServer.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }



    public static void verificarEMemoriaEAlertar(Computador computador, Double limiteAlerta) {
        String sql = "SELECT AVG((s.memoriaUso / c.memoriaTotal ) * 100) AS mediaMemoria FROM status_pc s JOIN tbComputador c ON c.idComputador = s.fkComputador WHERE fkComputador = ? AND dtHoraCaptura >= NOW() - INTERVAL 1  MINUTE;";

        String sqlServer = "SELECT AVG((s.memoriaUso / c.memoriaTotal ) * 100) AS mediaMemoria FROM status_pc s JOIN tbComputador c ON c.idComputador = s.fkComputador WHERE fkComputador = ? AND dtHoraCaptura >= DATEADD(MINUTE, -1, GETDATE());";

        PreparedStatement ps = null;
        ResultSet rs = null;

        PreparedStatement psSQLServer = null;
        ResultSet rsSQLServer = null;

        try {
            // Código para MySQL
            ps = Conexao.getConexao().prepareStatement(sql);
            ps.setString(1, computador.getId());

            rs = ps.executeQuery();

            if (rs.next()) {
                String mediaMemoriaStr = rs.getString("mediaMemoria");

                // Verificar se mediaMemoriaStr não é nulo
                if (mediaMemoriaStr != null) {
                    mediaMemoriaStr = mediaMemoriaStr.replace("%", "").trim(); // Remover o símbolo de percentagem e espaços extras
                    Double mediaMemoria = Double.parseDouble(mediaMemoriaStr);

                    // Usar um valor epsilon para lidar com a precisão dos números de ponto flutuante
                    double epsilon = 0.001;  // Ajuste conforme necessário
                    if (mediaMemoria > limiteAlerta + epsilon) {
                        Alerta alertaMemoria = new Alerta();
                        alertaMemoria.setDescricao("Uso elevado de memória RAM. Média de uso: %.2f%%".formatted(mediaMemoria));
                        alertaMemoria.setDtHoraAlerta(LocalDateTime.now().toString());
                        // Adicione outras informações ao alerta, se necessário
                        cadastrarAlerta(alertaMemoria, computador, "Memória RAM");
                    }
                } else {
                    // Tratar o caso em que mediaMemoriaStr é nulo, se necessário
                }
            }

            // Código para SQL Server
            psSQLServer = Conexao.getConexaoSQLServer().prepareStatement(sqlServer);
            psSQLServer.setString(1, computador.getId());

            rsSQLServer = psSQLServer.executeQuery();

            if (rsSQLServer.next()) {
                String mediaMemoriaStr = rsSQLServer.getString("mediaMemoria");

                // Verificar se mediaMemoriaStr não é nulo
                if (mediaMemoriaStr != null) {
                    mediaMemoriaStr = mediaMemoriaStr.replace("%", "").trim(); // Remover o símbolo de percentagem e espaços extras
                    Double mediaMemoria = Double.parseDouble(mediaMemoriaStr);

                    // Usar um valor epsilon para lidar com a precisão dos números de ponto flutuante
                    double epsilon = 0.001;  // Ajuste conforme necessário
                    if (mediaMemoria > limiteAlerta + epsilon) {
                        Alerta alertaMemoria = new Alerta();
                        alertaMemoria.setDescricao("Uso elevado de memória RAM. Média de uso: %.2f%%".formatted(mediaMemoria));
                        alertaMemoria.setDtHoraAlerta(LocalDateTime.now().toString());
                        // Adicione outras informações ao alerta, se necessário
                        cadastrarAlerta(alertaMemoria, computador, "Memória RAM");
                    }
                } else {
                    System.out.println("A coluna 'mediaMemoria' está com valor nulo.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // Fechar recursos (ResultSet, PreparedStatement, etc.)
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();

                if (rsSQLServer != null) rsSQLServer.close();
                if (psSQLServer != null) psSQLServer.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}