package br.com.projeto.comparador;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import br.com.projeto.comparador.model.registroPlanilha;
import br.com.projeto.comparador.reader.leitorPlanilha;

public class app {
    public static void main(String[] args) {
        System.out.println("=== SISTEMA DE COMPARAÇÃO DE PLANILHAS ===");
        System.out.println("Compara os dados do financeiro com os do cadastro");
        System.out.println("🔑 Utiliza CPF + Matrícula como chave de identificação\n");
        
        Scanner scanner = new Scanner(System.in);
        
        System.out.print("Digite o caminho da planilha do FINANCEIRO: ");
        String caminhoFinanceiro = scanner.nextLine();
        
        System.out.print("Digite o caminho da planilha do CADASTRO: ");
        String caminhoCadastro = scanner.nextLine();
        
        try {
            System.out.println("\n📂 Lendo planilha do FINANCEIRO...");
            List<registroPlanilha> financeiro = leitorPlanilha.ler(caminhoFinanceiro);
            System.out.println("   ✅ " + financeiro.size() + " registros lidos");
            
            System.out.println("📂 Lendo planilha do CADASTRO...");
            List<registroPlanilha> cadastro = leitorPlanilha.ler(caminhoCadastro);
            System.out.println("   ✅ " + cadastro.size() + " registros lidos");
            
            System.out.println("\n🔄 Comparando os dados...");
            comparadorPlanilhas.ResultadoComparacao resultado = comparadorPlanilhas.comparar(financeiro, cadastro);
            
            comparadorPlanilhas.exibirResultado(resultado);
            
            // Calcular totais
            int totalFinanceiro = financeiro.size();
            int totalCadastro = cadastro.size();
            
            // ==============================================
            // Gerar relatório Excel
            // ==============================================
            System.out.println("\n" + "-".repeat(50));
            System.out.print("📊 Deseja gerar relatório Excel? (s/n): ");
            String gerarExcel = scanner.nextLine();
            
            if (gerarExcel.equalsIgnoreCase("s")) {
                System.out.print("Digite o nome do arquivo de saída (ex: relatorio.xlsx): ");
                String nomeArquivo = scanner.nextLine();
                
                // Adicionar extensão .xlsx se não tiver
                if (!nomeArquivo.endsWith(".xlsx")) {
                    nomeArquivo += ".xlsx";
                }
                
                try {
                    // Agora com 6 parâmetros
                    geradorRelatorioExcel.gerar(resultado, caminhoFinanceiro, caminhoCadastro, nomeArquivo, totalFinanceiro, totalCadastro);
                    System.out.println("\n✅ Relatório Excel gerado com sucesso!");
                    System.out.println("📁 Arquivo salvo: " + nomeArquivo);
                } catch (IOException e) {
                    System.out.println("\n❌ Erro ao gerar relatório Excel: " + e.getMessage());
                }
            } else {
                System.out.println("❌ Relatório Excel não gerado.");
            }
            
        } catch (IOException e) {
            System.out.println("\n❌ ERRO ao ler as planilhas: " + e.getMessage());
            System.out.println("Verifique se os caminhos estão corretos e os arquivos existem.");
        }
        
        scanner.close();
    }
}