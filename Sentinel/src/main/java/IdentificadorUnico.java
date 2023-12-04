import java.util.UUID;

public class IdentificadorUnico {
    public static String gerarId(){
        // Gera um UUID aleatório (versão 4)
        UUID uuid = UUID.randomUUID();

        // Converte o UUID para uma representação de string
        String uuidString = uuid.toString();

        // Combina as informações para criar um identificador único
        String uniqueIdentifier = uuidString;
        return uniqueIdentifier;
    }
}
