package br.com.projeto.comparador.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
        return apenasNumeros(cpf);
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
    public static String normalizarNivelEstagio(String nivel) {
        return normalizarNome(nivel);
    }

    // Converte número do Excel para data
    private static LocalDate excelNumberToLocalDate(double excelDateNumber) {
        // Excel: 1 = 01/01/1900
        // Java LocalDate: 1900-01-01 = -657434? Ajuste manual
        long days = (long) excelDateNumber;
        // Subtrai 2 porque o Excel considera 1900 como ano bissexto (erro histórico)
        return LocalDate.of(1900, 1, 1).plusDays(days - 2);
    }
    
    // Converte string ou número para LocalDate
    public static LocalDate parseData(String dataStr) {
        if (dataStr == null || dataStr.trim().isEmpty()) return null;
        
        // Se for número (ex: 45658), converte como data do Excel
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
        
        // Tenta formatos de data
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
        return apenasNumeros(agencia);
    }
    
    // Normaliza conta (apenas números)
    public static String normalizarConta(String conta) {
        return apenasNumeros(conta);
    }
}