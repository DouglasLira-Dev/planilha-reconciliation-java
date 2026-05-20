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
     * @param resultado Resultado da comparação
     * @param caminhoFinanceiro Caminho da planilha financeiro
     * @param caminhoCadastro Caminho da planilha cadastro
     * @param arquivoSaida Caminho onde salvar o arquivo Excel
     * @param totalFinanceiro Total de registros do financeiro
     * @param totalCadastro Total de registros do cadastro
     * @throws IOException Erro ao salvar arquivo
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
            // Criar estilos
            criarEstilos(workbook);
            
            // Criar abas
            criarAbaResumo(workbook, resultado, caminhoFinanceiro, caminhoCadastro, totalFinanceiro, totalCadastro);
            criarAbaFaltantes(workbook, resultado);
            criarAbaExcedentes(workbook, resultado);
            criarAbaDivergencias(workbook, resultado);
            criarAbaConflitos(workbook, resultado);
            criarAbaPossiveisAbreviacoes(workbook, resultado);
            criarAbaCancelados(workbook, resultado);            // <-- NOVA ABA ADICIONADA
            criarAbaDetalhado(workbook, resultado);
            
            // Salvar arquivo
            try (FileOutputStream fileOut = new FileOutputStream(arquivoSaida)) {
                workbook.write(fileOut);
            }
        }
        
        System.out.println("✅ Relatório Excel gerado com sucesso: " + arquivoSaida);
    }
    
    private void criarEstilos(Workbook workbook) {
        // Título (negrito, tamanho 14)
        estiloTitulo = workbook.createCellStyle();
        Font fontTitulo = workbook.createFont();
        fontTitulo.setBold(true);
        fontTitulo.setFontHeightInPoints((short) 14);
        estiloTitulo.setFont(fontTitulo);
        
        // Cabeçalho (negrito, fundo cinza)
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
        
        // Erro (fundo vermelho)
        estiloErro = workbook.createCellStyle();
        estiloErro.setFillForegroundColor(IndexedColors.RED.getIndex());
        estiloErro.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloErro.setBorderBottom(BorderStyle.THIN);
        estiloErro.setBorderTop(BorderStyle.THIN);
        estiloErro.setBorderLeft(BorderStyle.THIN);
        estiloErro.setBorderRight(BorderStyle.THIN);
        
        // Aviso (fundo amarelo)
        estiloAviso = workbook.createCellStyle();
        estiloAviso.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        estiloAviso.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloAviso.setBorderBottom(BorderStyle.THIN);
        estiloAviso.setBorderTop(BorderStyle.THIN);
        estiloAviso.setBorderLeft(BorderStyle.THIN);
        estiloAviso.setBorderRight(BorderStyle.THIN);
        
        // Sucesso (fundo verde)
        estiloSucesso = workbook.createCellStyle();
        estiloSucesso.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        estiloSucesso.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Destaque (fundo azul claro)
        estiloDestaque = workbook.createCellStyle();
        estiloDestaque.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        estiloDestaque.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }
    
    private void criarAbaResumo(Workbook workbook, comparadorPlanilhas.ResultadoComparacao resultado, 
                                 String caminhoFinanceiro, String caminhoCadastro,
                                 int totalFinanceiro, int totalCadastro) {
        Sheet sheet = workbook.createSheet("01 - Resumo");
        int rowNum = 0;
        
        // Título
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("RELATÓRIO DE COMPARAÇÃO DE PLANILHAS");
        titleCell.setCellStyle(estiloTitulo);
        
        rowNum++; // linha em branco
        
        // Informações da comparação
        addInfoRow(sheet, rowNum++, "Data/Hora da geração:", 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        addInfoRow(sheet, rowNum++, "Planilha da Prévia:", caminhoFinanceiro);
        addInfoRow(sheet, rowNum++, "Planilha de Cadastro:", caminhoCadastro);
        addInfoRow(sheet, rowNum++, "Chave de comparação:", "CPF + Matrícula");
        addInfoRow(sheet, rowNum++, "Limiar similaridade de nomes:", 
            resultado.getConfiguracao().getLimiarSimilaridadeNomes() + "%");
        
        rowNum++; // linha em branco
        rowNum++; // linha em branco
        
        // Estatísticas
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
        // NOVA LINHA: Cancelados
        addInfoRow(sheet, rowNum++, "⚠️ Cancelados no cadastro (ignorados):", 
            String.valueOf(resultado.getTotalCancelados()));
        
        // Ajustar largura das colunas
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
        Sheet sheet = workbook.createSheet("02 - Registros que se encontram apenas na prévia");
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
    
    private void criarAbaExcedentes(Workbook workbook, comparadorPlanilhas.ResultadoComparacao resultado) {
        Sheet sheet = workbook.createSheet("03 - Registros que não estão na prévia");
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
        Sheet sheet = workbook.createSheet("04 - Divergências");
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
                // Pula se for uma possível abreviação (será listada em outra aba)
                if (div.getCampo().contains("abreviação")) {
                    continue;
                }

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
        Sheet sheet = workbook.createSheet("05 - Conflitos CPF e Matricula");
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

    private void criarAbaPossiveisAbreviacoes(Workbook workbook, comparadorPlanilhas.ResultadoComparacao resultado) {
        Sheet sheet = workbook.createSheet("06 - Possíveis Abreviações");
        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REGISTROS COM POSSÍVEIS ABREVIAÇÕES (NOMES)");
        titleCell.setCellStyle(estiloTitulo);

        rowNum++;

        // Contar quantas abreviações existem
        int totalAbreviacoes = 0;
        for (List<comparadorPlanilhas.Divergencia> list : resultado.getDivergenciasPorChave().values()) {
            for (comparadorPlanilhas.Divergencia div : list) {
                if (div.getCampo().contains("abreviação")) {
                    totalAbreviacoes++;
                }
            }
        }

        if (totalAbreviacoes == 0) {
            Row emptyRow = sheet.createRow(rowNum);
            emptyRow.createCell(0).setCellValue("Nenhuma possível abreviação encontrada.");
            return;
        }

        String[] headers = {"CPF", "Matrícula", "Campo", "Valor Financeiro", "Valor Cadastro", "Similaridade"};
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

            for (comparadorPlanilhas.Divergencia div : entry.getValue()) {
                if (div.getCampo().contains("abreviação")) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(cpf);
                    row.createCell(1).setCellValue(matricula);
                    row.createCell(2).setCellValue(div.getCampo());
                    row.createCell(3).setCellValue(div.getValorFinanceiro());
                    row.createCell(4).setCellValue(div.getValorCadastro());

                    Cell simCell = row.createCell(5);
                    if (div.getSimilaridade() != null) {
                        simCell.setCellValue(String.format("%.1f%%", div.getSimilaridade()));
                    } else {
                        simCell.setCellValue("N/A");
                    }
                    simCell.setCellStyle(estiloAviso);
                }
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.setColumnWidth(i, 5000);
        }
        sheet.setColumnWidth(5, 4000);
    }
    
    private void criarAbaCancelados(Workbook workbook, comparadorPlanilhas.ResultadoComparacao resultado) {
        Sheet sheet = workbook.createSheet("07 - Cancelados no Cadastro");
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
            // Exibe o valor original (pode ter "CANCELADO") - você pode armazená-lo se quiser, mas o campo dataInicioStr foi limpo.
            // Para manter o texto original, você precisaria guardar em uma variável separada. Vou deixar vazio como exemplo.
            row.createCell(4).setCellValue(reg.getDataInicioStr()); // Pode estar vazio após normalização
            row.createCell(5).setCellValue(reg.getDataFimStr());
            row.createCell(6).setCellValue(reg.getBanco());
            row.createCell(7).setCellValue(reg.getAgencia());
            row.createCell(8).setCellValue(reg.getConta());
            row.createCell(9).setCellValue("Registro cancelado (não comparado)");
            
            // Opcional: pintar a linha de amarelo para destaque
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
        Sheet sheet = workbook.createSheet("08 - Comparação Detalhada");
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