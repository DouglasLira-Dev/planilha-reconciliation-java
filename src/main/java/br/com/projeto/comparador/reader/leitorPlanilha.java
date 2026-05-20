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

    public static List<registroPlanilha> ler(String caminhoArquivo) throws IOException {
        List<registroPlanilha> lista = new ArrayList<>();
        
        try (Workbook workbook = WorkbookFactory.create(new File(caminhoArquivo))) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            
            if (!rowIterator.hasNext()) return lista;
            
            // Lê a primeira linha como cabeçalho
            Row headerRow = rowIterator.next();
            Map<String, Integer> colunaIndex = new HashMap<>();
            for (Cell cell : headerRow) {
                String nomeColuna = cell.getStringCellValue().trim().toLowerCase()
                        .replaceAll("[^a-z0-9]", ""); // normaliza para comparação
                colunaIndex.put(nomeColuna, cell.getColumnIndex());
            }
            
            // Exibe os cabeçalhos encontrados (para depuração)
            System.out.println("\n===== CABEÇALHOS DA PLANILHA (" + new File(caminhoArquivo).getName() + ") =====");
            for (Map.Entry<String, Integer> entry : colunaIndex.entrySet()) {
                System.out.println("  '" + entry.getKey() + "' -> coluna " + entry.getValue());
            }
            
            // Mapeia os índices usando sinônimos
            int idxMatricula = obterIndice(colunaIndex, "matricula", "matrícula", "numero", "prontuario", "contrato", "registro");
            int idxCpf = obterIndice(colunaIndex, "cpf", "cpfcnpj", "documento", "cpfcnpj");
            int idxNome = obterIndice(colunaIndex, "nome", "nomestagiario", "estagiario", "nomeestagiario", "nome abreviado", "nomeabreviado");
            int idxNivel = obterIndice(colunaIndex, "nivel","nível", "nivelestagio", "grau", "escolaridade", "nivelestagio", "estagio");
            int idxDataInicio = obterIndice(colunaIndex, "datainicio", "iniciocontrato", "data_inicio", "inicio", "dtinicio", "dt admissao", "dtadmissao", "teie");
            int idxDataFim = obterIndice(colunaIndex, "datafim", "fimcontrato", "data_fim", "fim", "dtfim","dt fim cont.", "dtfimcont.", "dt_prevtermino", "dtprevtermino");
            int idxBanco = obterIndice(colunaIndex, "banco", "codigobanco", "bancocodigo", "bco", "cod_banco", "codbanco");
            int idxAgencia = obterIndice(colunaIndex, "agencia", "agenciabanco", "nr_agencia");
            int idxConta = obterIndice(colunaIndex, "conta", "contacorrente", "numeroconta", "nr_conta", "c.corrente", "ccorrente");
            
            System.out.println("Índices mapeados:");
            System.out.println("  Matrícula: " + idxMatricula);
            System.out.println("  CPF: " + idxCpf);
            System.out.println("  Nome: " + idxNome);
            System.out.println("  Nível: " + idxNivel);
            System.out.println("  Data Início: " + idxDataInicio);
            System.out.println("  Data Fim: " + idxDataFim);
            System.out.println("  Banco: " + idxBanco);
            System.out.println("  Agência: " + idxAgencia);
            System.out.println("  Conta: " + idxConta);
            System.out.println("=======================================\n");
            
            // Agora percorre as linhas de dados
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                
                // Verifica se a linha está vazia (opcional)
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
                
                registroPlanilha registro = new registroPlanilha();
                
                registro.setMatricula(obterValorCelulaComIndice(row, idxMatricula));
                registro.setCpf(obterValorCelulaComIndice(row, idxCpf));
                registro.setNome(obterValorCelulaComIndice(row, idxNome));
                registro.setNivelEstagio(obterValorCelulaComIndice(row, idxNivel));
                registro.setDataInicioStr(obterValorCelulaComIndice(row, idxDataInicio));
                
                // ========== DETECÇÃO DE REGISTRO CANCELADO ==========
                String dataInicio = registro.getDataInicioStr();
                if (dataInicio != null && dataInicio.toLowerCase().contains("cancelado")) {
                    registro.setCancelado(true);
                    // Limpa a data para evitar erro no parseData (pois não é uma data válida)
                    registro.setDataInicioStr("");
                } else {
                    registro.setCancelado(false);
                }
                // ====================================================
                
                registro.setDataFimStr(obterValorCelulaComIndice(row, idxDataFim));
                registro.setBanco(obterValorCelulaComIndice(row, idxBanco));
                registro.setAgencia(obterValorCelulaComIndice(row, idxAgencia));
                registro.setConta(obterValorCelulaComIndice(row, idxConta));
                
                // Aplica normalização
                registro.setMatriculaNorm(normalizacao.normalizarMatricula(registro.getMatricula()));
                registro.setCpfNorm(normalizacao.normalizarCpf(registro.getCpf()));
                registro.setNomeNorm(normalizacao.normalizarNome(registro.getNome()));
                registro.setNivelEstagioNorm(normalizacao.normalizarNivelEstagio(registro.getNivelEstagio()));
                registro.setDataInicio(normalizacao.parseData(registro.getDataInicioStr()));
                registro.setDataFim(normalizacao.parseData(registro.getDataFimStr()));
                registro.setBancoNorm(normalizacao.normalizarBanco(registro.getBanco()));
                registro.setAgenciaNorm(normalizacao.normalizarAgencia(registro.getAgencia()));
                registro.setContaNorm(normalizacao.normalizarConta(registro.getConta()));
                
                // ========== IGNORAR LINHAS SEM CPF OU MATRÍCULA ==========
                if (registro.getMatriculaNorm().isEmpty() || registro.getCpfNorm().isEmpty()) {
                    System.out.println("  Linha ignorada: matrícula ou CPF vazio. Matrícula='" + registro.getMatricula() + "', CPF='" + registro.getCpf() + "'");
                    continue; // não adiciona à lista
                }
                // =======================================================
                
                lista.add(registro);
            }
        }
        
        return lista;
    }
    
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