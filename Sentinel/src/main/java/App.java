import DataAcessObject.*;
import Entidades.*;
import Logger.Mensageiro;
import com.github.britooo.looca.api.core.Looca;
import com.github.britooo.looca.api.group.discos.DiscoGrupo;
import com.github.britooo.looca.api.group.discos.Volume;
import com.github.britooo.looca.api.group.memoria.Memoria;
import com.github.britooo.looca.api.group.sistema.Sistema;
import com.github.britooo.looca.api.group.processador.Processador;
import com.github.britooo.looca.api.util.Conversor;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.CentralProcessor;
import oshi.hardware.Sensors;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static DataAcessObject.StatusPcDAO.verificarEAlertar;
import static DataAcessObject.StatusPcDAO.verificarEMemoriaEAlertar;

public class App {
    private static boolean alertaJaCadastrado = false;
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("""
                +===========================================================================================+ 
                ||                   ___   ____ __  ___        _   __ ____ _  __ ___   ____   __           ||
                ||                  / _ ) / __//  |/  / ____  | | / //  _// |/ // _ \\ / __ \\ / /           ||
                ||                 / _  |/ _/ / /|_/ / /___/  | |/ /_/ / /    // // // /_/ //_/\s           ||
                ||                /____//___//_/  /_/         |___//___//_/|_//____/ \\____/(_) \s           ||  
                ||                                                                             \s           ||            
                ||                                   ___   ____\s                                           ||    
                ||                                  / _ | / __ \\                                           ||                                                    
                ||                                 / __ |/ /_/ /                                           ||      
                ||                                /_/ |_|\\____/\s                                           ||    
                ||                                             \s                                           ||  
                ||                                                                                         ||            
                ||                   ____ ____ _  __ ______ ____ _  __ ____ __\s                            ||
                ||                  / __// __// |/ //_  __//  _// |/ // __// /\s                            ||
                ||                 _\\ \\ / _/ /    /  / /  _/ / /    // _/ / /__                            ||     
                ||                /___//___//_/|_/  /_/  /___//_/|_//___//____/                            ||
                ||                                                            \s                            ||  
                ||                                                                                         ||
                +==========================================================================================+
                """);

        String pattern =
                "                           @@@\n" +
                        "                               @@@@\n" +
                        "                                   @@@\n" +
                        "                                     @@@\n" +
                        "                                       @@@\n" +
                        "                                        @@@@\n" +
                        "                                         @@@@\n" +
                        "                                          @@@@\n" +
                        "                                           @@@@\n" +
                        "                                           @@@@@\n" +
                        "                                           @@@@@\n" +
                        "                                            @@@@\n" +
                        "                                           @@@@@\n" +
                        "                        @@@@@@@@@@@@       @@@@@@\n" +
                        "                     @@@@@@@@@@@@@@@      @@@@@@\n" +
                        "                  @@@@@@@@@@@@@@@@@@      @@@@@@\n" +
                        "                @@@@@@@@@@@@             @@@@@@@\n" +
                        "               @@@@@@                   @@@@@@@@\n" +
                        "             @@@@@@                    @@@@@@@@\n" +
                        "            @@@@@            @@@@      @@@@@@@\n" +
                        "           @@@@            @@@@@@@        @@\n" +
                        "          @@@@             @@@@@@@@@\n" +
                        "          @@                @@@@@@@@@\n" +
                        "          @@                  @@@@@@@@                             @\n" +
                        "         @@                     @@@@@@@@@                       @@\n" +
                        "         @@                       @@@@@@@@@@@                @@@\n" +
                        "         @@                          @@@@@@@@@@@@@     @@@@@@\n" +
                        "         @@                              @@@@@@@@@@@@@@@@\n" +
                        "          @\n" +
                        "          @\n";

        System.out.println(pattern);

        System.out.println("                   ____    ___  __  __  _____   _                                            ");
        System.out.println("                  |  _ \\  |_ _| \\ \\/ / | ____| | |                                        ");
        System.out.println("                  | |_) |  | |   \\  /  |  _|   | |                                          ");
        System.out.println("                  |  __/   | |   /  \\  | |___  | |___                                       ");
        System.out.println("                  |_|     |___| /_/\\_\\ |_____| |_____|                                     ");
        System.out.println("                                                                                             ");
        System.out.println(" __        __     _      _____    ____   _   _   _____   ____    ____  ");
        System.out.println(" \\ \\      / /    / \\    |_   _|  / ___| | | | | | ____| |  _ \\  / ___| ");
        System.out.println("  \\ \\  _ / /    / _ \\     | |   | |     | |_| | |  _|   | |_) | \\___ \\ ");
        System.out.println("   \\  V V /    / ___ \\    | |   | |___  |  _  | | |___  |  _ <   ___) |");
        System.out.println("    \\_\\/_/    /_/   \\_\\   |_|    \\____| |_| |_| |_____| |_| \\_\\ |____/ ");


        // objetos que foram criados na mão
        Looca looca = new Looca();
        Computador computador = new Computador();
        StatusPc idCaptura = new StatusPc();
        StatusPc memoriaUso = new StatusPc();
        StatusPc processadorUso = new StatusPc();
        StatusPc discoDisponivel = new StatusPc();
        StatusPc dtHoraCaptura = new StatusPc();
        Usuario usuario = new Usuario();
        Scanner entrada = new Scanner(System.in);
        Alerta alerta = new Alerta();

        boolean autenticado = false;

        // Objetos do looca
        Memoria memoria = looca.getMemoria();
        Processador processador = looca.getProcessador();
        DiscoGrupo disco = looca.getGrupoDeDiscos();
        Sistema sistema = looca.getSistema();


        // Variáveis que guardam as informações para o cadastro
        String nomeProcessador = processador.getNome();
        computador.setProcessador(nomeProcessador);

        String sistemaOperacional = sistema.getSistemaOperacional();
        computador.setSO(sistemaOperacional);


        Long memoriaTotal = (memoria.getTotal());
        computador.setMemoriaTot(memoriaTotal);

        Long discoTotal = (disco.getTamanhoTotal());
        computador.setDiscoTotal((discoTotal));

        Integer qtdDicos = disco.getVolumes().size();
        computador.setQtdDiscos(qtdDicos);

        do {
            System.out.println("Digite o usuario: ");
            String emailLogin = entrada.nextLine();

            System.out.println("Digite a senha: ");
            String senhaLogin = entrada.nextLine();

            UsuarioDAO.pegarUsuario(usuario);
            if (!Objects.equals(emailLogin, usuario.getEmail()) || !Objects.equals(senhaLogin, usuario.getSenha())) {
                System.out.println("usuario ou senha inválidos!");
                //Mensageiro.generateLog("ERRO - Usuário falhou ao se conectar com o sistema.");
            } else {
                //Mensageiro.generateLog("SUCESSO - Usuário titular do email: " + emailLogin + " logou no sistema");
                System.out.println("Usuário encontrado!");

                UsuarioDAO.pegarEmpresaUsuario(usuario);
                ArenaDAO.pegarArenasDaEmpresa(usuario);
                if (ArenaDAO.pegarArenasDaEmpresa(usuario).isEmpty()) {
                    System.out.println("Você ainda não tem uma arena cadastrada, acesse nosso site para fazer isso");
                    return;
                } else {
                    System.out.println("Digite o apelido do computador:");
                    String apelido = entrada.next();
                    if (!ComputadorDAO.JaExiste(computador ,apelido)) {
                        System.out.println();
                        System.out.println("Parece que essa é a primeira vez que você utiliza o Sentinel nesse PC");
                        System.out.println("Em qual arena você deseja cadastrar esse computador?");
                        System.out.println();
                        for (int i = 0; i < ArenaDAO.pegarArenasDaEmpresa(usuario).size(); i++) {
                            if (i == ArenaDAO.pegarArenasDaEmpresa(usuario).size() - 1) {
                                System.out.println("""
                                        +----------------------------------------------------
                                        | %d - %s
                                        +----------------------------------------------------"""
                                        .formatted(i + 1, ArenaDAO.pegarArenasDaEmpresa(usuario).get(i)));
                            } else {
                                System.out.println("""
                                        +----------------------------------------------------
                                        | %d - %s""".formatted(i + 1, ArenaDAO.pegarArenasDaEmpresa(usuario).get(i)));
                            }
                        }
                        Integer numArena = entrada.nextInt();
                        String nomeArena = ArenaDAO.pegarArenasDaEmpresa(usuario).get(numArena - 1);

                        IdentificadorUnico identificadorUnico = new IdentificadorUnico();

                        String idPC = identificadorUnico.gerarId();
                        computador.setId(idPC);

                        ComputadorDAO.cadastrarComputador(computador, apelido, nomeArena);
                    }


                    computador.gerarTextoInicio();
                    StatusPcDAO.pegarIdCaptura(idCaptura);
                    StatusPcDAO.exibirInformacoesMaquina(nomeProcessador, sistemaOperacional, memoriaTotal, discoTotal, qtdDicos);


                    Integer pontosMontagem = disco.getVolumes().size();
                    long TEMPO = (2000);
                    Timer timer = new Timer();
                    TimerTask tarefa = new TimerTask() {
                        @Override
                        public void run() {
                            try {
                                Long memoriaEmUso = memoria.getEmUso();
                                memoriaUso.setMemoriaUso(memoriaEmUso);

                                Double processadorEmUso = processador.getUso();
                                processadorUso.setProcessadorEmUso(processadorEmUso);

                                Long discoEmUso = disco.getVolumes().get(0).getDisponivel();
                                discoDisponivel.setDiscoDisponivel(discoEmUso);

                                StatusPcDAO.cadastrarCapturas(memoriaUso, processadorUso, discoDisponivel, dtHoraCaptura, computador);
                                Integer verificacaoDisco = 0;

                                if (disco.getVolumes().size() > pontosMontagem) {
                                    verificacaoDisco = 1;
                                    System.out.println("ATENÇÃO!\nDISCO DESCONHECIDO CONECTADO ");

                                    // Verifica se o alerta já foi cadastrado antes
                                    if (verificacaoDisco == 1 && !alertaJaCadastrado) {
                                        // Cadastra um alerta quando verificacaoDisco é igual a 1
                                        Alerta alerta = new Alerta();
                                        System.out.println("                _   _____  _____  _   _   ____     _      ___     _                ");
                                        System.out.println("               / \\ |_   _|| ____|| \\ | | / ___|   / \\    / _ \\   | |               ");
                                        System.out.println("              / _ \\  | |  |  _|  |  \\| || |      / _ \\  | | | |  | |               ");
                                        System.out.println("             / ___ \\ | |  | |___ | |\\  || |___  / ___ \\ | |_| |  |_|               ");
                                        System.out.println("            /_/   \\_\\|_|  |_____||_| \\_| \\____|/_/   \\_  \\___/   (_)               ");
                                        System.out.println("                                                                                   ");
                                        System.out.println("                         ____  ___  ____    ____  ___                           ");
                                        System.out.println("                        |  _ \\|_ _|/ ___|  / ___|/ _ \\                          ");
                                        System.out.println("                        | | | || | \\___ \\ | |   | | | |                         ");
                                        System.out.println("                        | |_| || |  ___) || |___| |_| |                         ");
                                        System.out.println("                        |____/|___||____/  \\____|\\___/                          ");
                                        System.out.println("                                                                                   ");
                                        System.out.println("  ____   _____  ____    ____  ___   _   _  _   _  _____  ____  ___  ____    ___  ");
                                        System.out.println(" |  _ \\ | ____|/ ___|  / ___|/ _ \\ | \\ | || | | || ____|/ ___||_ _||  _ \\  / _ \\ ");
                                        System.out.println(" | | | ||  _|  \\___ \\ | |   | | | ||  \\| || |_| ||  _| | |     | | | | | || | | |");
                                        System.out.println(" | |_| || |___  ___) || |___| |_| || |\\  ||  _  || |___| |___  | | | |_| || |_| |");
                                        System.out.println(" |____/ |_____||____/  \\____|\\___/ |_| \\_||_| |_||_____|\\____||___||____/  \\___/ \n");


                                        alerta.setCaminhoArquivo("N/A"); // Você pode ajustar conforme necessário
                                        alerta.setDtHoraAlerta(String.valueOf(LocalDateTime.now()));
                                        String tipoAlerta = "Disco Desconhecido";

                                        // Chama o método cadastrarAlerta da classe AlertaDAO
                                        AlertaDAO.cadastrarAlerta(alerta, computador, tipoAlerta);
                                        alertaJaCadastrado = true;  // Define que o alerta foi cadastrado
                                        System.out.println("CADASTROU O ALERTA DE DISCO DESCONHECIDO");
                                    }
                                } else {
                                    System.out.println("QUANTIDADE DE DISCOS ESTÁ DE ACORDO :)");
                                    // Reinicia a variável de controle quando não há mais alerta
                                    alertaJaCadastrado = false;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    timer.scheduleAtFixedRate(tarefa, TEMPO, TEMPO);
                    autenticado = true;

                    ProgramScanner programScanner = new ProgramScanner();

                    System.out.println("COMEÇOU O PROCESSO DE VERIFICAÇÃO DE ARQUIVOS E PASTAS PROIBIDOS");

                    try {
                        programScanner.scanForBlacklistedApps();
                        // Se chegou aqui, nenhum programa proibido foi encontrado
                        System.out.println(" __  __     _     ___   _   _  ___  _   _     _     ");
                        System.out.println("|  \\/  |   / \\   / _ \\ | | | ||_ _|| \\ | |   / \\    ");
                        System.out.println("| |\\/| |  / _ \\ | | | || | | | | | |  \\| |  / _ \\   ");
                        System.out.println("| |  | | / ___ \\| |_| || |_| | | | | |\\  | / ___ \\  ");
                        System.out.println("|_|  |_|/_/   \\_ \\__\\_\\ \\___/ |___||_| \\_|/_/   \\_\\ ");
                        System.out.println("                                                    ");
                        System.out.println("     ____   _____  ____  _   _  ____    ___         ");
                        System.out.println("    / ___| | ____|/ ___|| | | ||  _ \\  / _ \\        ");
                        System.out.println("    \\___ \\ |  _| | |  _ | | | || |_) || | | |       ");
                        System.out.println("     ___) || |___| |_| || |_| ||  _ < | |_| |       ");
                        System.out.println("    |____/ |_____|\\____| \\___/ |_| \\_\\ \\___/        ");
                        System.out.println("                                                    ");
                        System.out.println(" ____    _     ____      _      _   _  ____    ___  ");
                        System.out.println("|  _ \\  / \\   |  _ \\    / \\    | | | |/ ___|  / _ \\ ");
                        System.out.println("| |_) |/ _ \\  | |_) |  / _ \\   | | | |\\___ \\ | | | |");
                        System.out.println("|  __// ___ \\ |  _ <  / ___ \\  | |_| | ___) || |_| |");
                        System.out.println("|_|  /_/   \\_\\|_| \\_\\/_/   \\_\\  \\___/ |____/  \\___/ \n");                    } catch (ProgramScanner.ProgramProibidoEncontradoException e) {
                        System.out.println(e.getMessage());

                        LocalDateTime data = LocalDateTime.now();
                        alerta.setDtHoraAlerta(String.valueOf(data));
                        alerta.setCaminhoArquivo(programScanner.getAbsoluteFilePath());

                        String tipoAlerta;
                        if (e.getMessage().startsWith("Pasta proibida")) {
                            tipoAlerta = "Pasta Proibida";
                            alerta.setDescricao("Pasta proibida encontrada");
                            System.out.println("            _    ____   ___  _   _ _____     _____             \n" +
                                    "           / \\  |  _ \\ / _ \\| | | |_ _\\ \\   / / _ \\            \n" +
                                    "          / _ \\ | |_) | | | | | | || | \\ \\ / / | | |           \n" +
                                    "         / ___ \\|  _ <| |_| | |_| || |  \\ V /| |_| |           \n" +
                                    "        /_/__ \\_\\_| \\_\\\\__\\_\\\\___/|___|__\\_/  \\___/            \n" +
                                    "        |  _ \\|  _ \\ / _ \\_ _| __ )_ _|  _ \\ / _ \\             \n" +
                                    "        | |_) | |_) | | | | ||  _ \\| || | | | | | |            \n" +
                                    "        |  __/|  _ <| |_| | || |_) | || |_| | |_| |            \n" +
                                    " _____ _|_|_  |_|_\\_\\\\___/___|____/___|____/ \\___/ ____   ___  \n" +
                                    "| ____| \\ | |/ ___/ _ \\| \\ | |_   _|  _ \\    / \\  |  _ \\ / _ \\ \n" +
                                    "|  _| |  \\| | |  | | | |  \\| | | | | |_) |  / _ \\ | | | | | | |\n" +
                                    "| |___| |\\  | |__| |_| | |\\  | | | |  _ <  / ___ \\| |_| | |_| |\n" +
                                    "|_____|_| \\_|\\____\\___/|_| \\_| |_| |_| \\_\\/_/   \\_\\____/ \\___/ ");
                        } else if (e.getMessage().startsWith("Arquivo proibido")) {
                            tipoAlerta = "Arquivo Proibido";
                            alerta.setDescricao("Arquivo proibido encontrado");
                            System.out.println("            _    ____   ___  _   _ _____     _____             \n" +
                                    "           / \\  |  _ \\ / _ \\| | | |_ _\\ \\   / / _ \\            \n" +
                                    "          / _ \\ | |_) | | | | | | || | \\ \\ / / | | |           \n" +
                                    "         / ___ \\|  _ <| |_| | |_| || |  \\ V /| |_| |           \n" +
                                    "        /_/__ \\_\\_| \\_\\\\__\\_\\\\___/|___|__\\_/  \\___/            \n" +
                                    "        |  _ \\|  _ \\ / _ \\_ _| __ )_ _|  _ \\ / _ \\             \n" +
                                    "        | |_) | |_) | | | | ||  _ \\| || | | | | | |            \n" +
                                    "        |  __/|  _ <| |_| | || |_) | || |_| | |_| |            \n" +
                                    " _____ _|_|_  |_|_\\_\\\\___/___|____/___|____/ \\___/ ____   ___  \n" +
                                    "| ____| \\ | |/ ___/ _ \\| \\ | |_   _|  _ \\    / \\  |  _ \\ / _ \\ \n" +
                                    "|  _| |  \\| | |  | | | |  \\| | | | | |_) |  / _ \\ | | | | | | |\n" +
                                    "| |___| |\\  | |__| |_| | |\\  | | | |  _ <  / ___ \\| |_| | |_| |\n" +
                                    "|_____|_| \\_|\\____\\___/|_| \\_| |_| |_| \\_\\/_/   \\_\\____/ \\___/ ");
                        } else {
                            tipoAlerta = "Desconhecido";
                            alerta.setDescricao("Alerta desconhecido encontrado");
                        }

                        // Aqui deve ser o id do computador
                        AlertaDAO.cadastrarAlerta(alerta, computador, tipoAlerta);


                        System.out.println("                             _____  ___  ___");
                        System.out.println("                            |  ___|/ _ \\|_ _|");
                        System.out.println("                            | |_  | | | || |");
                        System.out.println("                            |  _| | |_| || |");
                        System.out.println("                            |_|    \\___/|___|");
                        System.out.println("                                             ");
                        System.out.println("  ____     _     ____     _     ____  _____  ____      _     ____    ___");
                        System.out.println(" / ___|   / \\   |  _ \\   / \\   / ___||_   _||  _ \\    / \\   |  _ \\  / _ \\");
                        System.out.println("| |      / _ \\  | | | | / _ \\  \\___ \\  | |  | |_) |  / _ \\  | | | || | | |");
                        System.out.println("| |___  / ___ \\ | |_| |/ ___ \\  ___) | | |  |  _ <  / ___ \\ | |_| || |_| |");
                        System.out.println(" \\____|/_/   \\_\\|____//_/   \\\\_|____/  |_|  |_| \\_\\/_/   \\_\\|____/  \\___/");
                        System.out.println("                                             ");
                        System.out.println("              ___       _     _      _____  ____  _____   _");
                        System.out.println("             / _ \\     / \\   | |    | ____||  _ \\|_   _| / \\");
                        System.out.println("            | | | |   / _ \\  | |    |  _|  | |_) | | |  / _ \\");
                        System.out.println("            | |_| |  / ___ \\ | |___ | |___ |  _ <  | | / ___ \\");
                        System.out.println("             \\___/  /_/   \\_\\|_____||_____||_| \\_\\ |_|/_/   \\_\\");
                        System.out.println("                                             ");
                        System.out.println("     ____    ___       _     ____    ___   _   _  ___ __     __ ___");
                        System.out.println("    |  _ \\  / _ \\     / \\   |  _ \\  / _ \\ | | | ||_ _|\\ \\   / // _ \\");
                        System.out.println("    | | | || | | |   / _ \\  | |_) || | | || | | | | |  \\ \\ / /| | | |");
                        System.out.println("    | |_| || |_| |  / ___ \\ |  _ < | |_| || |_| | | |   \\ V / | |_| |");
                        System.out.println("    |____/  \\___/  /_/   \\_\\|_| \\_\\ \\__\\_\\ \\___/ |___|   \\_/   \\___/");
                        System.out.println("                                             ");
                        System.out.println("             ____   ____    ___  ___  ____  ___  ____    ___");
                        System.out.println("            |  _ \\ |  _ \\  / _ \\|_ _|| __ )|_ _||  _ \\  / _ \\");
                        System.out.println("            | |_) || |_) || | | || | |  _ \\ | | | | | || | | |");
                        System.out.println("            |  __/ |  _ < | |_| || | | |_) || | | |_| || |_| |");
                        System.out.println("            |_|    |_| \\_\\ \\___/|___||____/|___||____/  \\___/\n");
                    }
                }

                Timer timer = new Timer();
                TimerTask tarefaVerificacao = new TimerTask() {
                    @Override
                    public void run() {
                        verificarEAlertar(computador, 90.0);  // 90.0 é o limite de alerta, ajuste conforme necessário
                    }
                };
                long intervalo = 1 * 65 * 1000; // 1 minuto em milissegundos
                timer.scheduleAtFixedRate(tarefaVerificacao, intervalo, intervalo);

                Timer timerMemoria = new Timer();
                TimerTask tarefaVerificacaoMemoria = new TimerTask() {
                    @Override
                    public void run() {
                        verificarEMemoriaEAlertar(computador, 90.0);  // 90.0 é o limite de alerta, ajuste conforme necessário
                    }
                };
                long intervaloMemoria = 1 * 70 * 1000; // 1 minuto e 30 segundos em milissegundos
                timerMemoria.scheduleAtFixedRate(tarefaVerificacaoMemoria, intervaloMemoria, intervaloMemoria);

            }
        }
        while (!autenticado);
    }
}