package br.com.projeto.comparador.reader;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
            
            // Pular cabeçalho
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }
            
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                
                // Verificar se a linha está vazia
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
                
                registro.setMatricula(obterValorCelula(row.getCell(0)));
                registro.setCpf(obterValorCelula(row.getCell(1)));
                registro.setNome(obterValorCelula(row.getCell(2)));
                registro.setNivelEstagio(obterValorCelula(row.getCell(3)));
                registro.setDataInicioStr(obterValorCelula(row.getCell(4)));
                registro.setDataFimStr(obterValorCelula(row.getCell(5)));
                registro.setBanco(obterValorCelula(row.getCell(6)));
                registro.setAgencia(obterValorCelula(row.getCell(7)));
                registro.setConta(obterValorCelula(row.getCell(8)));
                
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
                
                lista.add(registro);
            }
        }
        
        return lista;
    }
    
    private static String obterValorCelula(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
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