package br.com.projeto.comparador;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import br.com.projeto.comparador.model.registroPlanilha;
import br.com.projeto.comparador.reader.leitorPlanilha;

public class app {
    public static void main(String[] args) {
        System.out.println("=== SISTEMA DE COMPARAÇÃO DE PLANILHAS ===");
        System.out.println("Compara os dados do financeiro com os do cadastro\n");
        
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
            
        } catch (IOException e) {
            System.out.println("\n❌ ERRO ao ler as planilhas: " + e.getMessage());
            System.out.println("Verifique se os caminhos estão corretos e os arquivos existem.");
        }
        
        scanner.close();
    }
}