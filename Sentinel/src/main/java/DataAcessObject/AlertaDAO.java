package DataAcessObject;

import Conexao.Conexao;
import Entidades.Alerta;
import Entidades.Computador;
import Slack.Slack;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AlertaDAO {

    static JSONObject json = new JSONObject();
    static String textoAlerta;

    public static boolean cadastrarAlerta(Alerta alerta, Computador computador, String tipoAlerta) throws IOException, InterruptedException {
        String sql = "INSERT INTO Alerta (descricao, dtHoraAlerta, caminhoArquivo, tipoAlerta, fkComputador) " +
                "VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = null;
        PreparedStatement psSQLServer = null;

        try {
            ps = Conexao.getConexao().prepareStatement(sql);
            ps.setString(1, alerta.getDescricao());
            ps.setString(2, alerta.getDtHoraAlerta());
            ps.setString(3, alerta.getCaminhoArquivo());
            ps.setString(4, tipoAlerta);  // Tipo de alerta (Pasta ou Arquivo)
            ps.setString(5, computador.getId());
            psSQLServer = Conexao.getConexaoSQLServer().prepareStatement(sql);
            psSQLServer.setString(1, alerta.getDescricao());
            psSQLServer.setString(2, alerta.getDtHoraAlerta());
            psSQLServer.setString(3, alerta.getCaminhoArquivo());
            psSQLServer.setString(4, tipoAlerta);  // Tipo de alerta (Pasta ou Arquivo)
            psSQLServer.setString(5, computador.getId());

            Integer rowsAffected = ps.executeUpdate();
            Integer rowsAffectedSQLServer = psSQLServer.executeUpdate();

            textoAlerta = String.format("""
                    ALERTA NO MONITORAMENTO!
                    Descrição: %s
                    Data e Hora: %s
                    Caminho do arquivo: %s
                    Tipo de alerta: %s
                    Computador: %s
                    """, alerta.getDescricao(), alerta.getDtHoraAlerta(), alerta.getCaminhoArquivo(), tipoAlerta, computador.getId());

            json.put("text", textoAlerta);
            Slack.sendMessage(json);

            return rowsAffected > 0 && rowsAffectedSQLServer > 0; // Retorna verdadeiro se pelo menos uma linha for afetada

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Em caso de falha
    }
}
