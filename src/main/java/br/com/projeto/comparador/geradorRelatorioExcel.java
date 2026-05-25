package br.com.projeto.comparador;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class geradorRelatorioExcel {
    
    // Estilos de célula
    private CellStyle estiloTitulo;
    private CellStyle estiloCabecalho;
    private CellStyle estiloErro;
    private CellStyle estiloAviso;
    private CellStyle estiloSucesso;
    private CellStyle estiloDestaque;
    
    /**
     * Gera o relatório completo em Excel
     */
    public static void gerar(comparadorPlanilhas.ResultadoComparacao resultado, 
                             String caminhoFinanceiro, 
                             String caminhoCadastro, 
                             String arquivoSaida,
                             int totalFinanceiro, 
                             int totalCadastro) throws IOException {
        geradorRelatorioExcel gerador = new geradorRelatorioExcel();
        gerador.criarRelatorio(resultado, caminhoFinanceiro, caminhoCadastro, arquivoSaida, totalFinanceiro, totalCadastro);
    }
    
    private void criarRelatorio(comparadorPlanilhas.ResultadoComparacao resultado, 
                                String caminhoFinanceiro, 
                                String caminhoCadastro, 
                                String arquivoSaida,
                                int totalFinanceiro, 
                                int totalCadastro) throws IOException {
        
        try (Workbook workbook = new XSSFWorkbook()) {
            criarEstilos(workbook);
            
            criarAbaResumo(workbook, resultado, caminhoFinanceiro, caminhoCadastro, totalFinanceiro, totalCadastro);
            criarAbaConformes(workbook, resultado);
            criarAbaFaltantes(workbook, resultado);
            criarAbaExcedentes(workbook, resultado);
            criarAbaDivergencias(workbook, resultado);
            criarAbaConflitos(workbook, resultado);
            criarAbaPossiveisAbreviacoes(workbook, resultado); // aba modificada
            criarAbaCancelados(workbook, resultado);            
            criarAbaDetalhado(workbook, resultado);
                          
            try (FileOutputStream fileOut = new FileOutputStream(arquivoSaida)) {
                workbook.write(fileOut);
            }
        }
        
        System.out.println("✅ Relatório Excel gerado com sucesso: " + arquivoSaida);
    }
    
    private void criarEstilos(Workbook workbook) {
        estiloTitulo = workbook.createCellStyle();
        Font fontTitulo = workbook.createFont();
        fontTitulo.setBold(true);
        fontTitulo.setFontHeightInPoints((short) 14);
        estiloTitulo.setFont(fontTitulo);
        
        estiloCabecalho = workbook.createCellStyle();
        Font fontCabecalho = workbook.createFont();
        fontCabecalho.setBold(true);
        estiloCabecalho.setFont(fontCabecalho);
        estiloCabecalho.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        estiloCabecalho.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloCabecalho.setBorderBottom(BorderStyle.THIN);
        estiloCabecalho.setBorderTop(BorderStyle.THIN);
        estiloCabecalho.setBorderLeft(BorderStyle.THIN);
        estiloCabecalho.setBorderRight(BorderStyle.THIN);
        
        estiloErro = workbook.createCellStyle();
        estiloErro.setFillForegroundColor(IndexedColors.RED.getIndex());
        estiloErro.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloErro.setBorderBottom(BorderStyle.THIN);
        estiloErro.setBorderTop(BorderStyle.THIN);
        estiloErro.setBorderLeft(BorderStyle.THIN);
        estiloErro.setBorderRight(BorderStyle.THIN);
        
        estiloAviso = workbook.createCellStyle();
        estiloAviso.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        estiloAviso.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloAviso.setBorderBottom(BorderStyle.THIN);
        estiloAviso.setBorderTop(BorderStyle.THIN);
        estiloAviso.setBorderLeft(BorderStyle.THIN);
        estiloAviso.setBorderRight(BorderStyle.THIN);
        
        estiloSucesso = workbook.createCellStyle();
        estiloSucesso.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        estiloSucesso.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        estiloDestaque = workbook.createCellStyle();
        estiloDestaque.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        estiloDestaque.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }
    
    private void criarAbaResumo(Workbook workbook, comparadorPlanilhas.ResultadoComparacao resultado, 
                                 String caminhoFinanceiro, String caminhoCadastro,
                                 int totalFinanceiro, int totalCadastro) {
        Sheet sheet = workbook.createSheet("01 - Resumo");
        int rowNum = 0;
        
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("RELATÓRIO DE COMPARAÇÃO DE PLANILHAS");
        titleCell.setCellStyle(estiloTitulo);
        
        rowNum++;
        
        addInfoRow(sheet, rowNum++, "Data/Hora da geração:", 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        addInfoRow(sheet, rowNum++, "Planilha da Prévia:", caminhoFinanceiro);
        addInfoRow(sheet, rowNum++, "Planilha de Cadastro:", caminhoCadastro);
        addInfoRow(sheet, rowNum++, "Chave de comparação:", "CPF + Matrícula");
        addInfoRow(sheet, rowNum++, "Limiar similaridade de nomes:", 
            resultado.getConfiguracao().getLimiarSimilaridadeNomes() + "%");
        
        rowNum++;
        rowNum++;
        
        addInfoRow(sheet, rowNum++, "📊 ESTATÍSTICAS DA COMPARAÇÃO", "");
        addInfoRow(sheet, rowNum++, "", "");
        addInfoRow(sheet, rowNum++, "Total de Registros da Prévia:", String.valueOf(totalFinanceiro));
        addInfoRow(sheet, rowNum++, "Total de Registros do Cadastro:", String.valueOf(totalCadastro));
        addInfoRow(sheet, rowNum++, "", "");
        addInfoRow(sheet, rowNum++, "✅ Registros conformes (idênticos):", 
            String.valueOf(resultado.getTotalConformes()));
        addInfoRow(sheet, rowNum++, "❌ Registros que se encontram apenas na Prévia:", 
            String.valueOf(resultado.getTotalFaltantes()));
        addInfoRow(sheet, rowNum++, "⚠️ Registros que não estão na Prévia:", 
            String.valueOf(resultado.getTotalExcedentes()));
        addInfoRow(sheet, rowNum++, "🔄 Registros com divergências:", 
            String.valueOf(resultado.getTotalDivergencias()));
        addInfoRow(sheet, rowNum++, "   ├── ❌ Erros:", 
            String.valueOf(resultado.getTotalErros()));
        addInfoRow(sheet, rowNum++, "   └── ⚠️ Avisos:", 
            String.valueOf(resultado.getTotalAvisos()));
        addInfoRow(sheet, rowNum++, "⚡ Conflitos (CPF igual, matrícula diferente):", 
            String.valueOf(resultado.getTotalConflitos()));
        addInfoRow(sheet, rowNum++, "⚠️ Cancelados no cadastro (ignorados):", 
            String.valueOf(resultado.getTotalCancelados()));
        
        sheet.setColumnWidth(0, 8000);
        sheet.setColumnWidth(1, 6000);
    }
    
    private void addInfoRow(Sheet sheet, int rowNum, String label, String value) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(estiloCabecalho);
        
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(estiloDestaque);
    }
    
    private void criarAbaFaltantes(Workbook workbook, comparadorPlanilhas.ResultadoComparacao resultado) {
        Sheet sheet = workbook.createSheet("03 - Registros que se encontram apenas na prévia");
        int rowNum = 0;
        
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REGISTROS QUE SE ENCONTRAM APENAS NA PRÉVIA (não estão na planilha de cadastro)");
        titleCell.setCellStyle(estiloTitulo);
        
        rowNum++;
        
        if (resultado.getTotalFaltantes() == 0) {
            Row emptyRow = sheet.createRow(rowNum);
            emptyRow.createCell(0).setCellValue("Nenhum registro inconsistente encontrado.");
            return;
        }
        
        String[] headers = {"Matrícula", "CPF", "Nome", "Nível Estágio", "Data Início", 
                           "Data Fim", "Banco", "Agência", "Conta"};
        Row headerRow = sheet.createRow(rowNum++);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(estiloCabecalho);
        }
        
        for (var reg : resultado.getFaltantesNoCadastro()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(reg.getMatricula());
            row.createCell(1).setCellValue(reg.getCpf());
            row.createCell(2).setCellValue(reg.getNome());
            row.createCell(3).setCellValue(reg.getNivelEstagio());
            row.createCell(4).setCellValue(reg.getDataInicioStr());
            row.createCell(5).setCellValue(reg.getDataFimStr());
            row.createCell(6).setCellValue(reg.getBanco());
            row.createCell(7).setCellValue(reg.getAgencia());
            row.createCell(8).setCellValue(reg.getConta());
        }
        
        for (int i = 0; i < headers.length; i++) {
            sheet.setColumnWidth(i, 5000);
        }
    }
    
    private void criarAbaConformes(Workbook workbook, comparadorPlanilhas.ResultadoComparacao resultado) {
        Sheet sheet = workbook.createSheet("02 - Registros Conformes");
        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REGISTROS CONFORMES (IDÊNTICOS NAS DUAS PLANILHAS)");
        titleCell.setCellStyle(estiloTitulo);

        rowNum++;

        if (resultado.getConformes().isEmpty()) {
            Row emptyRow = sheet.createRow(rowNum);
            emptyRow.createCell(0).setCellValue("Nenhum registro conforme encontrado.");
            return;
        }

        String[] headers = {"Matrícula", "CPF", "Nome", "Nível Estágio", "Data Início", 
                            "Data Fim", "Banco", "Agência", "Conta"};
        Row headerRow = sheet.createRow(rowNum++);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(estiloCabecalho);
        }

        for (var reg : resultado.getConformes()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(reg.getMatricula());
            row.createCell(1).setCellValue(reg.getCpf());
            row.createCell(2).setCellValue(reg.getNome());
            row.createCell(3).setCellValue(reg.getNivelEstagio());
            row.createCell(4).setCellValue(reg.getDataInicioStr());
            row.createCell(5).setCellValue(reg.getDataFimStr());
            row.createCell(6).setCellValue(reg.getBanco());
            row.createCell(7).setCellValue(reg.getAgencia());
            row.createCell(8).setCellValue(reg.getConta());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.setColumnWidth(i, 5000);
        }
    }
    
    private void criarAbaExcedentes(Workbook workbook, comparadorPlanilhas.ResultadoComparacao resultado) {
        Sheet sheet = workbook.createSheet("04 - Registros que não estão na prévia");
        int rowNum = 0;
        
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REGISTROS QUE NÃO ESTÃO NA PRÉVIA (FALTAM CADASTRAR)");
        titleCell.setCellStyle(estiloTitulo);
        
        rowNum++;
        
        if (resultado.getTotalExcedentes() == 0) {
            Row emptyRow = sheet.createRow(rowNum);
            emptyRow.createCell(0).setCellValue("Nenhum registro inconsistente encontrado.");
            return;
        }
        
        String[] headers = {"Matrícula", "CPF", "Nome", "Nível Estágio", "Data Início", 
                           "Data Fim", "Banco", "Agência", "Conta"};
        Row headerRow = sheet.createRow(rowNum++);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(estiloCabecalho);
        }
        
        for (var reg : resultado.getExcedentesNoCadastro()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(reg.getMatricula());
            row.createCell(1).setCellValue(reg.getCpf());
            row.createCell(2).setCellValue(reg.getNome());
            row.createCell(3).setCellValue(reg.getNivelEstagio());
            row.createCell(4).setCellValue(reg.getDataInicioStr());
            row.createCell(5).setCellValue(reg.getDataFimStr());
            row.createCell(6).setCellValue(reg.getBanco());
            row.createCell(7).setCellValue(reg.getAgencia());
            row.createCell(8).setCellValue(reg.getConta());
        }
        
        for (int i = 0; i < headers.length; i++) {
            sheet.setColumnWidth(i, 5000);
        }
    }
    
    private void criarAbaDivergencias(Workbook workbook, comparadorPlanilhas.ResultadoComparacao resultado) {
        Sheet sheet = workbook.createSheet("05 - Divergências");
        int rowNum = 0;
        
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("DIVERGÊNCIAS ENCONTRADAS");
        titleCell.setCellStyle(estiloTitulo);
        
        rowNum++;
        
        if (resultado.getTotalDivergencias() == 0) {
            Row emptyRow = sheet.createRow(rowNum);
            emptyRow.createCell(0).setCellValue("Nenhuma divergência encontrada.");
            return;
        }
        
        String[] headers = {"CPF", "Matrícula", "Campo", "Valor Financeiro", "Valor Cadastro", "Tipo"};
        Row headerRow = sheet.createRow(rowNum++);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(estiloCabecalho);
        }
        
        for (Map.Entry<String, List<comparadorPlanilhas.Divergencia>> entry : resultado.getDivergenciasPorChave().entrySet()) {
            String[] partes = entry.getKey().split("\\|");
            String cpf = partes.length > 0 ? partes[0] : "";
            String matricula = partes.length > 1 ? partes[1] : "";
            
            for (var div : entry.getValue()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(cpf);
                row.createCell(1).setCellValue(matricula);
                row.createCell(2).setCellValue(div.getCampo());
                row.createCell(3).setCellValue(div.getValorFinanceiro());
                row.createCell(4).setCellValue(div.getValorCadastro());
                
                Cell tipoCell = row.createCell(5);
                tipoCell.setCellValue(div.getTipo());
                if (div.getTipo().equals("ERRO")) {
                    tipoCell.setCellStyle(estiloErro);
                } else if (div.getTipo().equals("AVISO")) {
                    tipoCell.setCellStyle(estiloAviso);
                }
            }
        }
        
        for (int i = 0; i < headers.length; i++) {
            sheet.setColumnWidth(i, 5000);
        }
    }
    
    private void criarAbaConflitos(Workbook workbook, comparadorPlanilhas.ResultadoComparacao resultado) {
        Sheet sheet = workbook.createSheet("06 - Conflitos CPF e Matricula");
        int rowNum = 0;
        
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("CONFLITOS (MESMO CPF COM MATRÍCULA DIFERENTE)");
        titleCell.setCellStyle(estiloTitulo);
        
        rowNum++;
        
        if (resultado.getTotalConflitos() == 0) {
            Row emptyRow = sheet.createRow(rowNum);
            emptyRow.createCell(0).setCellValue("Nenhum conflito encontrado.");
            return;
        }
        
        String[] headers = {"Matrícula", "CPF", "Nome", "Nível Estágio", "Data Início", 
                           "Data Fim", "Banco", "Agência", "Conta", "Observação"};
        Row headerRow = sheet.createRow(rowNum++);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(estiloCabecalho);
        }
        
        for (var reg : resultado.getConflitosCPFMatricula()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(reg.getMatricula());
            row.createCell(1).setCellValue(reg.getCpf());
            row.createCell(2).setCellValue(reg.getNome());
            row.createCell(3).setCellValue(reg.getNivelEstagio());
            row.createCell(4).setCellValue(reg.getDataInicioStr());
            row.createCell(5).setCellValue(reg.getDataFimStr());
            row.createCell(6).setCellValue(reg.getBanco());
            row.createCell(7).setCellValue(reg.getAgencia());
            row.createCell(8).setCellValue(reg.getConta());
            row.createCell(9).setCellValue("Verificar manualmente - mesmo CPF com matrícula diferente");
            
            for (int col = 0; col < 10; col++) {
                if (row.getCell(col) == null) {
                    row.createCell(col);
                }
                row.getCell(col).setCellStyle(estiloErro);
            }
        }
        
        for (int i = 0; i < headers.length; i++) {
            sheet.setColumnWidth(i, 5000);
        }
        sheet.setColumnWidth(9, 10000);
    }

    // ================== MÉTODO MODIFICADO ==================
    private void criarAbaPossiveisAbreviacoes(Workbook workbook, comparadorPlanilhas.ResultadoComparacao resultado) {
        Sheet sheet = workbook.createSheet("07 - Possíveis Abreviações");
        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REGISTROS COM POSSÍVEIS ABREVIAÇÕES (CONSIDERADOS CONFORMES)");
        titleCell.setCellStyle(estiloTitulo);

        rowNum++;

        // Obtém a lista de PossivelAbreviatura (pares financeiro/cadastro)
        var abreviacoes = resultado.getPossiveisAbreviacoes();
        if (abreviacoes == null || abreviacoes.isEmpty()) {
            Row emptyRow = sheet.createRow(rowNum);
            emptyRow.createCell(0).setCellValue("Nenhuma possível abreviação encontrada.");
            return;
        }

        // Cabeçalho com duas colunas de nome: Financeiro (abreviado) e Cadastro (completo)
        String[] headers = {"Matrícula", "CPF", "Nome Financeiro (abreviado)", "Nome Cadastro (completo)", 
                            "Similaridade", "Nível Estágio", "Data Início", "Data Fim", "Banco", "Agência", "Conta"};
        Row headerRow = sheet.createRow(rowNum++);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(estiloCabecalho);
        }

        for (var abv : abreviacoes) {
            var fin = abv.getFinanceiro();
            var cad = abv.getCadastro();
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(fin.getMatricula());
            row.createCell(1).setCellValue(fin.getCpf());
            row.createCell(2).setCellValue(fin.getNome());          // nome abreviado do financeiro
            row.createCell(3).setCellValue(cad.getNome());          // nome completo do cadastro
            row.createCell(4).setCellValue(String.format("%.1f%%", abv.getSimilaridade()));
            row.createCell(5).setCellValue(cad.getNivelEstagio());
            row.createCell(6).setCellValue(cad.getDataInicioStr());
            row.createCell(7).setCellValue(cad.getDataFimStr());
            row.createCell(8).setCellValue(cad.getBanco());
            row.createCell(9).setCellValue(cad.getAgencia());
            row.createCell(10).setCellValue(cad.getConta());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.setColumnWidth(i, i == 4 ? 4000 : 5000);
        }
        sheet.setColumnWidth(2, 6000); // nome financeiro
        sheet.setColumnWidth(3, 6000); // nome cadastro
    }
    
    private void criarAbaCancelados(Workbook workbook, comparadorPlanilhas.ResultadoComparacao resultado) {
        Sheet sheet = workbook.createSheet("08 - Cancelados no Cadastro");
        int rowNum = 0;
    
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REGISTROS CANCELADOS (ignorados na comparação)");
        titleCell.setCellStyle(estiloTitulo);
    
        rowNum++;
    
        if (resultado.getTotalCancelados() == 0) {
            Row emptyRow = sheet.createRow(rowNum);
            emptyRow.createCell(0).setCellValue("Nenhum registro cancelado encontrado.");
            return;
        }
    
        String[] headers = {"Matrícula", "CPF", "Nome", "Nível Estágio", "Data Início (original)", 
                        "Data Fim", "Banco", "Agência", "Conta", "Observação"};
        Row headerRow = sheet.createRow(rowNum++);
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(estiloCabecalho);
        }
    
        for (var reg : resultado.getCanceladosNoCadastro()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(reg.getMatricula());
            row.createCell(1).setCellValue(reg.getCpf());
            row.createCell(2).setCellValue(reg.getNome());
            row.createCell(3).setCellValue(reg.getNivelEstagio());
            row.createCell(4).setCellValue(reg.getDataInicioStr());
            row.createCell(5).setCellValue(reg.getDataFimStr());
            row.createCell(6).setCellValue(reg.getBanco());
            row.createCell(7).setCellValue(reg.getAgencia());
            row.createCell(8).setCellValue(reg.getConta());
            row.createCell(9).setCellValue("Registro cancelado (não comparado)");
            
            for (int i = 0; i < headers.length; i++) {
                if (row.getCell(i) == null) row.createCell(i);
                row.getCell(i).setCellStyle(estiloAviso);
            }
        }
    
        for (int i = 0; i < headers.length; i++) {
            sheet.setColumnWidth(i, 5000);
        }
        sheet.setColumnWidth(9, 8000);
    }  
    
    private void criarAbaDetalhado(Workbook workbook, comparadorPlanilhas.ResultadoComparacao resultado) {
        Sheet sheet = workbook.createSheet("09 - Comparação Detalhada");
        int rowNum = 0;
        
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("COMPARAÇÃO DETALHADA");
        titleCell.setCellStyle(estiloTitulo);
        
        rowNum++;
        
        String[] headers = {"Status", "Matrícula", "CPF", "Nome", "Nível Estágio", 
                           "Data Início", "Data Fim", "Banco", "Agência", "Conta"};
        Row headerRow = sheet.createRow(rowNum++);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(estiloCabecalho);
        }
        
        Row infoRow = sheet.createRow(rowNum);
        infoRow.createCell(0).setCellValue("Aba em desenvolvimento - Em breve mostrará todos os registros com seus respectivos status.");
        
        for (int i = 0; i < headers.length; i++) {
            sheet.setColumnWidth(i, 5000);
        }
    }
}