package br.com.projeto.comparador.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.text.Normalizer;

public class normalizacao {

    // Remove tudo que não é número (CPF, agência, conta)
    public static String apenasNumeros(String valor) {
        if (valor == null) return "";
        return valor.replaceAll("[^0-9]", "");
    }

    // Normaliza matrícula: remove espaços extras
    public static String normalizarMatricula(String matricula) {
        if (matricula == null) return "";
        return matricula.trim().replaceAll("\\s+", " ");
    }

    // Normaliza CPF (apenas números)
    public static String normalizarCpf(String cpf) {
    // Remove tudo que não é número
    String numeros = apenasNumeros(cpf);
    if (numeros.isEmpty()) return "";
    
    // Garante 11 dígitos com zeros à esquerda
    while (numeros.length() < 11) {
        numeros = "0" + numeros;
    }
    // Se por acaso tiver mais de 11 dígitos, corta (raro, mas seguro)
    if (numeros.length() > 11) {
        numeros = numeros.substring(0, 11);
    }
    return numeros;
}

    // Normaliza nome: remove acentos, converte para minúsculo, remove pontuação
    public static String normalizarNome(String nome) {
        if (nome == null) return "";
        String normalized = Normalizer.normalize(nome, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        normalized = normalized.toLowerCase();
        normalized = normalized.replaceAll("[^a-z0-9\\s]", "");
        normalized = normalized.replaceAll("\\s+", " ").trim();
        return normalized;
    }

    // Normaliza nível de estágio (similar ao nome)
    private static final Map<String, String> MAPA_NIVEIS = new HashMap<>();
    static {
        MAPA_NIVEIS.put("formprof", "medio");
        MAPA_NIVEIS.put("formacao profissional", "medio");
        MAPA_NIVEIS.put("ensinomedio", "medio");
        MAPA_NIVEIS.put("superior", "superior");
        // ...
     }

    public static String normalizarNivelEstagio(String nivel) {
        if (nivel == null) return "";
        String norm = normalizarNome(nivel);
        return MAPA_NIVEIS.getOrDefault(norm, norm);
    }

    // Converte número do Excel para data
    private static LocalDate excelNumberToLocalDate(double excelDateNumber) {
        long days = (long) excelDateNumber;
        return LocalDate.of(1900, 1, 1).plusDays(days - 2);
    }
    
    // Converte string ou número para LocalDate
    public static LocalDate parseData(String dataStr) {
        if (dataStr == null || dataStr.trim().isEmpty()) return null;
        
        if (dataStr.matches("\\d+")) {
            try {
                long number = Long.parseLong(dataStr);
                if (number > 0 && number < 100000) {
                    return excelNumberToLocalDate(number);
                }
            } catch (NumberFormatException e) {
                // Não é número, continua
            }
        }
        
        DateTimeFormatter[] formatters = {
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy")
        };
        for (DateTimeFormatter fmt : formatters) {
            try {
                return LocalDate.parse(dataStr.trim(), fmt);
            } catch (DateTimeParseException e) {
                // tenta o próximo formato
            }
        }
        return null;
    }
    
    // Normaliza banco (apenas números)
    public static String normalizarBanco(String banco) {
        return apenasNumeros(banco);
    }
    
    // Normaliza agência (apenas números)
    public static String normalizarAgencia(String agencia) {
    String numeros = apenasNumeros(agencia);
    if (numeros.isEmpty()) return "";
    
    // Padroniza para 5 dígitos (tamanho comum para agências, ex: Bradesco 0000-0 -> 00000)
    // Se a agência tiver menos de 5 dígitos, adiciona zeros à esquerda.
    while (numeros.length() < 5) {
        numeros = "0" + numeros;
    }
    // Se tiver mais de 5 (raro), mantém os primeiros 5 (ou pode cortar, a critério)
    if (numeros.length() > 5) {
        numeros = numeros.substring(0, 5);
    }
    return numeros;
}
    
    // Normaliza conta (apenas números)
    public static String normalizarConta(String conta) {
        return apenasNumeros(conta);
    }
    
    // ========== MÉTODOS PARA COMPARAÇÃO DE NOMES ==========
    
    /**
     * Calcula a distância de Levenshtein entre duas strings
     */
    public static int distanciaLevenshtein(String s1, String s2) {
        if (s1 == null || s2 == null) return 0;
        
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    int custo = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                    dp[i][j] = Math.min(Math.min(dp[i-1][j] + 1, dp[i][j-1] + 1), dp[i-1][j-1] + custo);
                }
            }
        }
        return dp[s1.length()][s2.length()];
    }
    
    /**
     * Calcula o percentual de similaridade entre duas strings
     */
    public static double similaridade(String s1, String s2) {
        if (s1 == null || s2 == null) return 0;
        if (s1.isEmpty() && s2.isEmpty()) return 100;
        
        int distancia = distanciaLevenshtein(s1, s2);
        int maxLen = Math.max(s1.length(), s2.length());
        
        if (maxLen == 0) return 100;
        return (1.0 - (double) distancia / maxLen) * 100;
    }
    
    /**
     * Verifica se dois nomes têm diferença de apenas algumas letras (erro de digitação)
     */
    public static boolean isErroDigitacao(String nome1, String nome2) {
        if (nome1 == null || nome2 == null) return false;
        
        int distancia = distanciaLevenshtein(nome1, nome2);
        
        // Se a diferença é de apenas 1 ou 2 caracteres, é erro de digitação
        return distancia <= 2 && distancia > 0;
    }
    
    /**
     * Verifica se um nome é abreviação do outro
     */
    public static boolean isAbreviacao(String nome1, String nome2) {
        if (nome1 == null || nome2 == null) return false;
        
        String[] palavras1 = nome1.split(" ");
        String[] palavras2 = nome2.split(" ");
        
        int palavrasComuns = 0;
        int palavrasSignificativas = 0;
        
        for (String p1 : palavras1) {
            if (p1.length() > 2) {
                palavrasSignificativas++;
                for (String p2 : palavras2) {
                    if (p2.contains(p1) || p1.contains(p2)) {
                        palavrasComuns++;
                        break;
                    }
                }
            }
        }
        
        // Se mais de 60% das palavras significativas são comuns, pode ser abreviação
        return palavrasSignificativas > 0 && 
               (double) palavrasComuns / palavrasSignificativas >= 0.6;
    }
}