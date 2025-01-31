import java.util.Arrays;

public class PipelineOACRiscV {
    // Banco de registradores
    private static final int[] registradores = new int[32];
    // Memória simulada
    private static final int[] memoria = new int[256];
    // Registradores do pipeline

    // Contador de programa
    private static int pc = 0;

    private static String ifId, idEx, exMem, memWb;
    private static int idExA, idExB, exMemResultado, memWbValor;

    public static void main(String[] args) {
        inicializar();
        // Conjunto de instruções simuladas
        String[] instrucoes = {
                "00000000001000110000100000110011",
                "00000000010000100000100010010011",
                "00000000100001000000010001100011",
                "00000000001000110000110010110011",
                "00000000001101010000001001100011",
                "00000001111011100010001000100011"
        };

        // Simulação do pipeline por ciclos de clock
        for (int ciclo = 0; ciclo < instrucoes.length + 4; ciclo++) {
            System.out.println("Ciclo: " + ciclo);
            escritaDeVolta();
            acessoMemoria();
            executar();
            decodificar();
            buscar(instrucoes);
            imprimirEstado();
        }
    }

    // Inicializa registradores e pipeline
    private static void inicializar() {
        ifId = idEx = exMem = memWb = "NOP";
        for (int i = 0; i < 32; i++) {
            registradores[i] = i;
        }
    }

    // Estágio IF: Busca de instrução
    private static void buscar(String[] instrucoes) {
        if (pc / 4 < instrucoes.length) {
            ifId = instrucoes[pc / 4];
            pc += 4;
        } else {
            ifId = "NOP";
        }
    }

    // Estágio ID: Decodificação da instrução
    private static void decodificar() {
        if (!ifId.equals("NOP")) {
            int rs1 = Integer.parseInt(ifId.substring(12, 17), 2);
            int rs2 = Integer.parseInt(ifId.substring(7, 12), 2);
            idExA = registradores[rs1];
            idExB = registradores[rs2];
            idEx = ifId;
        } else {
            idEx = "NOP";
        }
    }

    // Estágio EX: Execução da instrução
    private static void executar() {
        if (!idEx.equals("NOP")) {
            String opcode = idEx.substring(25, 32);
            int imediato = Integer.parseInt(idEx.substring(0, 12), 2);

            switch (opcode) {
                case "0110011": // Instruções do tipo R (ex: add)
                    exMemResultado = idExA + idExB;
                    break;
                case "0010011": // Instruções do tipo I (ex: addi)
                    exMemResultado = idExA + imediato;
                    break;
                case "0100011": // Instruções do tipo S (ex: sw)
                    exMemResultado = idExA + imediato;
                    break;
                case "1100011": // Instruções do tipo B (ex: beq)
                    if (idExA == idExB) {
                        pc += imediato - 8;
                        System.out.println("Desvio tomado. PC atualizado para: " + pc);
                        ifId = "NOP";
                        idEx = "NOP";
                    }
                    break;
            }
            exMem = idEx;
        } else {
            exMem = "NOP";
        }
    }

    // Estágio MEM: Acesso à memória
    private static void acessoMemoria() {
        if (!exMem.equals("NOP")) {
            String opcode = exMem.substring(25, 32);
            int rs2 = Integer.parseInt(exMem.substring(7, 12), 2);

            if (opcode.equals("0100011")) { // Instruções store (SW)
                if (exMemResultado >= 0 && exMemResultado < memoria.length) {
                    memoria[exMemResultado] = registradores[rs2];
                } else {
                    System.out.println("Endereço de memória inválido para SW: " + exMemResultado);
                }
            } else {
                memWbValor = exMemResultado;
            }
            memWb = exMem;
        } else {
            memWb = "NOP";
        }
    }

    // Estágio WB: Escrita no banco de registradores
    private static void escritaDeVolta() {
        if (!memWb.equals("NOP")) {
            String opcode = memWb.substring(25, 32);
            int rd = Integer.parseInt(memWb.substring(20, 25), 2);

            if (opcode.equals("0110011") || opcode.equals("0010011")) { // Tipos R e I
                registradores[rd] = memWbValor;
            }
        }
    }

    // Imprime o estado atual do pipeline
    private static void imprimirEstado() {
        System.out.println("PC: " + pc);
        System.out.println("Registradores: " + Arrays.toString(registradores));
        System.out.println("Memória: " + Arrays.toString(memoria));
        System.out.println("IF/ID: " + ifId);
        System.out.println("ID/EX: " + idEx + ", A: " + idExA + ", B: " + idExB);
        System.out.println("EX/MEM: " + exMem + ", Resultado ALU: " + exMemResultado);
        System.out.println("MEM/WB: " + memWb + ", Valor WB: " + memWbValor);
        System.out.println("============================================================");
    }
}
