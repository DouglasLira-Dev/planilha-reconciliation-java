package br.com.projeto.comparador.reader;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import br.com.projeto.comparador.model.registroPlanilha;
import br.com.projeto.comparador.util.normalizacao;

public class leitorPlanilha {

    /**
     * Lê a primeira aba da planilha (comportamento original).
     * @param caminhoArquivo caminho do arquivo Excel
     * @return lista de registros da primeira aba
     * @throws IOException erro de leitura
     */
    public static List<registroPlanilha> ler(String caminhoArquivo) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(new File(caminhoArquivo))) {
            Sheet sheet = workbook.getSheetAt(0);
            return lerAba(sheet, caminhoArquivo, workbook.getSheetName(0));
        }
    }

    /**
     * Lê múltiplas abas da planilha e consolida os registros.
     * @param caminhoArquivo caminho do arquivo Excel
     * @param nomesAbas lista de nomes das abas a serem lidas (se nula ou vazia, lê todas as abas de dados)
     * @return lista consolidada de registros (ordem conforme as abas)
     * @throws IOException erro de leitura
     */
    public static List<registroPlanilha> ler(String caminhoArquivo, List<String> nomesAbas) throws IOException {
        List<registroPlanilha> listaConsolidada = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(new File(caminhoArquivo))) {
            if (nomesAbas == null || nomesAbas.isEmpty()) {
                // Lê todas as abas que forem consideradas "dados"
                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    String nomeAba = workbook.getSheetName(i);
                    if (isAbaDados(nomeAba)) {
                        Sheet sheet = workbook.getSheetAt(i);
                        listaConsolidada.addAll(lerAba(sheet, caminhoArquivo, nomeAba));
                    }
                }
            } else {
                // Lê apenas as abas especificadas
                for (String nomeAba : nomesAbas) {
                    Sheet sheet = workbook.getSheet(nomeAba);
                    if (sheet == null) {
                        System.err.println("Aba não encontrada: " + nomeAba);
                        continue;
                    }
                    listaConsolidada.addAll(lerAba(sheet, caminhoArquivo, nomeAba));
                }
            }
        }
        return listaConsolidada;
    }

    /**
     * Retorna os nomes de todas as abas da planilha (para exibição na interface).
     * @param caminhoArquivo caminho do arquivo Excel
     * @return lista de nomes das abas (todas, inclusive as de legenda)
     * @throws IOException erro de leitura
     */
    public static List<String> listarNomesAbas(String caminhoArquivo) throws IOException {
        List<String> nomes = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(new File(caminhoArquivo))) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                nomes.add(workbook.getSheetName(i));
            }
        }
        return nomes;
    }

    /**
     * Retorna apenas os nomes das abas que são consideradas "dados" (ignora legenda, instrução, etc.)
     * @param caminhoArquivo caminho do arquivo Excel
     * @return lista de nomes de abas de dados
     * @throws IOException erro de leitura
     */
    public static List<String> listarNomesAbasDados(String caminhoArquivo) throws IOException {
        List<String> nomes = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(new File(caminhoArquivo))) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                String nome = workbook.getSheetName(i);
                if (isAbaDados(nome)) {
                    nomes.add(nome);
                }
            }
        }
        return nomes;
    }

    /**
     * Verifica se o nome da aba indica que ela contém dados (não é legenda, instrução, etc.)
     */
    public static boolean isAbaDados(String nomeAba) {
        String nomeLower = nomeAba.toLowerCase();
        return !(nomeLower.contains("legenda") || nomeLower.contains("instrução") 
                || nomeLower.contains("resumo") || nomeLower.contains("capa")
                || nomeLower.contains("índice") || nomeLower.startsWith("readme"));
    }

    /**
     * Lê uma única aba e retorna seus registros.
     */
    private static List<registroPlanilha> lerAba(Sheet sheet, String nomeArquivo, String nomeAba) throws IOException {
        List<registroPlanilha> lista = new ArrayList<>();
        Iterator<Row> rowIterator = sheet.iterator();
        if (!rowIterator.hasNext()) return lista;

        // Cabeçalho
        Row headerRow = rowIterator.next();
        Map<String, Integer> colunaIndex = new HashMap<>();
        for (Cell cell : headerRow) {
            String nomeColuna = cell.getStringCellValue().trim().toLowerCase()
                    .replaceAll("[^a-z0-9]", "");
            colunaIndex.put(nomeColuna, cell.getColumnIndex());
        }

        // Mapeia índices (usando os mesmos sinônimos do método original)
        int idxMatricula = obterIndice(colunaIndex, "matricula", "matrícula", "numero", "prontuario", "contrato", "registro");
        int idxCpf = obterIndice(colunaIndex, "cpf", "cpfcnpj", "documento");
        int idxNome = obterIndice(colunaIndex, "nome", "nomestagiario", "estagiario", "nomeestagiario", "nome abreviado", "nomeabreviado");
        int idxNivel = obterIndice(colunaIndex,  "nivel", "nivelestagio", "grau", "escolaridade", "estagio",
    "niveldeestagio", "nivel_estagio", "nivel-estagio", "niveldoestagio", "estagionivel", "nvel");
        int idxDataInicio = obterIndice(colunaIndex, "datainicio", "iniciocontrato", "data_inicio", "inicio", "dtinicio", "dt admissao", "dtadmissao", "teie");
        int idxDataFim = obterIndice(colunaIndex, "datafim", "fimcontrato", "data_fim", "fim", "dtfim","dt fim cont.", "dtfimcont.", "dt_prevtermino", "dtprevtermino");
        int idxBanco = obterIndice(colunaIndex, "banco", "codigobanco", "bancocodigo", "bco", "cod_banco", "codbanco");
        int idxAgencia = obterIndice(colunaIndex, "agencia", "agenciabanco", "nr_agencia");
        int idxConta = obterIndice(colunaIndex, "conta", "contacorrente", "numeroconta", "nr_conta", "c.corrente", "ccorrente");

        System.out.println("Lendo aba: " + nomeAba + " (arquivo: " + nomeArquivo + ")");

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            // Ignora linha completamente vazia (opcional)
            boolean linhaVazia = true;
            for (int i = 0; i < 9; i++) {
                Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                String valor = obterValorCelula(cell);
                if (valor != null && !valor.isEmpty()) {
                    linhaVazia = false;
                    break;
                }
            }
            if (linhaVazia) continue;

            registroPlanilha registro = lerLinha(row, idxMatricula, idxCpf, idxNome, idxNivel, idxDataInicio, idxDataFim, idxBanco, idxAgencia, idxConta);
            if (registro != null) {
                lista.add(registro);
            }
        }
        return lista;
    }

    /**
     * Lê uma única linha da planilha e retorna um registroPlanilha devidamente populado e normalizado.
     * Retorna null se a linha for inválida (matrícula ou CPF vazios após normalização).
     */
    private static registroPlanilha lerLinha(Row row, 
                                             int idxMatricula, int idxCpf, int idxNome, int idxNivel,
                                             int idxDataInicio, int idxDataFim, int idxBanco, int idxAgencia, int idxConta) {
        registroPlanilha registro = new registroPlanilha();
        registro.setMatricula(obterValorCelulaComIndice(row, idxMatricula));
        registro.setCpf(obterValorCelulaComIndice(row, idxCpf));
        registro.setNome(obterValorCelulaComIndice(row, idxNome));
        registro.setNivelEstagio(obterValorCelulaComIndice(row, idxNivel));
        registro.setDataInicioStr(obterValorCelulaComIndice(row, idxDataInicio));

        // Detecta cancelado
        String dataInicio = registro.getDataInicioStr();
        if (dataInicio != null && dataInicio.toLowerCase().contains("cancelado")) {
            registro.setCancelado(true);
            registro.setDataInicioStr("");
        } else {
            registro.setCancelado(false);
        }

        registro.setDataFimStr(obterValorCelulaComIndice(row, idxDataFim));
        registro.setBanco(obterValorCelulaComIndice(row, idxBanco));
        registro.setAgencia(obterValorCelulaComIndice(row, idxAgencia));
        registro.setConta(obterValorCelulaComIndice(row, idxConta));

        // Normaliza
        registro.setMatriculaNorm(normalizacao.normalizarMatricula(registro.getMatricula()));
        registro.setCpfNorm(normalizacao.normalizarCpf(registro.getCpf()));
        registro.setNomeNorm(normalizacao.normalizarNome(registro.getNome()));
        registro.setNivelEstagioNorm(normalizacao.normalizarNivelEstagio(registro.getNivelEstagio()));
        registro.setDataInicio(normalizacao.parseData(registro.getDataInicioStr()));
        registro.setDataFim(normalizacao.parseData(registro.getDataFimStr()));
        registro.setBancoNorm(normalizacao.normalizarBanco(registro.getBanco()));
        registro.setAgenciaNorm(normalizacao.normalizarAgencia(registro.getAgencia()));
        registro.setContaNorm(normalizacao.normalizarConta(registro.getConta()));

        // Ignora linha sem matrícula ou CPF
        if (registro.getMatriculaNorm().isEmpty() || registro.getCpfNorm().isEmpty()) {
            return null;
        }
        return registro;
    }

    // Métodos auxiliares (obterIndice, obterValorCelulaComIndice, obterValorCelula) – já existem, mantenha como estão
    // ... copie os métodos existentes do seu leitorPlanilha original (obterIndice, obterValorCelulaComIndice, obterValorCelula) para cá.

    private static int obterIndice(Map<String, Integer> mapa, String... possiveisNomes) {
        for (String nome : possiveisNomes) {
            String chave = nome.toLowerCase().replaceAll("[^a-z0-9]", "");
            if (mapa.containsKey(chave)) {
                return mapa.get(chave);
            }
        }
        return -1;
    }

    private static String obterValorCelulaComIndice(Row row, int idx) {
        if (idx < 0) return "";
        Cell cell = row.getCell(idx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        return obterValorCelula(cell);
    }

    private static String obterValorCelula(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    try {
                        return cell.getLocalDateTimeCellValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    } catch (Exception e) {
                        return cell.getDateCellValue().toString();
                    }
                }
                double valor = cell.getNumericCellValue();
                if (valor == Math.floor(valor)) {
                    return String.valueOf((long) valor);
                } else {
                    return String.valueOf(valor);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (IllegalStateException e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }
}