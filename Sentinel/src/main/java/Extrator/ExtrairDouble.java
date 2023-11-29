package Extrator;

public class ExtrairDouble {
    public static double extrairNumero(String inputString) {
        // Remover caracteres não numéricos usando expressão regular
        String parteNumerica = inputString.replaceAll("[^0-9.,]", "");

        // Substituir vírgulas por pontos para lidar com números decimais
        parteNumerica = parteNumerica.replace(",", ".");

        // Converter a parte numérica para double
        return Double.parseDouble(parteNumerica);
    }
}
