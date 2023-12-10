package DataAcessObject;
import Conexao.Conexao;
import Entidades.Alerta;
import Entidades.Computador;
import Extrator.ExtrairDouble;
import com.github.britooo.looca.api.util.Conversor;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.github.britooo.looca.api.util.Conversor.formatarBytes;

public class ComputadorDAO {
    Integer idComputador = 0;
    Computador computador = new Computador();
    public static boolean cadastrarComputador (Computador computador, String nomePC, String arena){
        String sql = " INSERT INTO tbComputador (idComputador, apelidoPc, sistemaOperacional, processador, discoTotal, memoriaTotal, qtdDiscos, fkArena) VALUES (?, ?, ?, ?, ?, ?, ?, (SELECT idArena FROM tbArena WHERE nomeArena = ?))";
        PreparedStatement ps = null;

        PreparedStatement psSQLServer = null;
        try{
            ps = Conexao.getConexao().prepareStatement(sql);
            ps.setString(1, computador.getId());
            ps.setString(2, nomePC);
            ps.setString(3, computador.getSO());
            ps.setString(4, computador.getProcessador());
            ps.setDouble(5, ExtrairDouble.extrairNumero(Conversor.formatarBytes(computador.getDiscoTotal())));
            ps.setDouble(6, ExtrairDouble.extrairNumero(Conversor.formatarBytes(computador.getMemoriaTot())));
            ps.setInt(7, computador.getQtdDiscos());
            ps.setString(8,arena);
            ps.execute();

            psSQLServer = Conexao.getConexaoSQLServer().prepareStatement(sql);
            psSQLServer.setString(1, computador.getId());
            psSQLServer.setString(2, nomePC);
            psSQLServer.setString(3, computador.getSO());
            psSQLServer.setString(4, computador.getProcessador());
            psSQLServer.setDouble(5, ExtrairDouble.extrairNumero(Conversor.formatarBytes(computador.getDiscoTotal())));;
            psSQLServer.setDouble(6, ExtrairDouble.extrairNumero(Conversor.formatarBytes(computador.getMemoriaTot())));
            psSQLServer.setInt(7, computador.getQtdDiscos());
            psSQLServer.setString(8, arena);
            psSQLServer.execute();
        } catch (SQLException e ){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return false;
    }

    public static Boolean JaExiste(Computador computador ,String apelido) {
        String sql = "SELECT COUNT(*) as count, MAX(idComputador) as idComputador FROM tbComputador WHERE apelidoPc = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;

        PreparedStatement psSQLServer = null;
        ResultSet rsSQLServer = null;

        try {
            ps = Conexao.getConexao().prepareStatement(sql);
            ps.setString(1, apelido);

            rs = ps.executeQuery();

            // Verificar se há algum resultado
            if (rs.next()) {
                int count = rs.getInt("count");
                if (count > 0) {
                    computador.setId(rs.getString("idComputador"));
                    return true;
                } else {
                    return false;
                }
            }

            psSQLServer = Conexao.getConexaoSQLServer().prepareStatement(sql);
            psSQLServer.setString(1, apelido);

            rsSQLServer = psSQLServer.executeQuery();

            // Verificar se há algum resultado
            if (rsSQLServer.next()) {
                int count = rsSQLServer.getInt("count");
                if (count > 0){
                    computador.setId(rs.getString("idComputador"));
                    return true;
                }
                else{
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rsSQLServer != null) {
                    rsSQLServer.close();
                }
                if (psSQLServer != null) {
                    psSQLServer.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}