package br.com.projeto.comparador.model;

import java.time.LocalDate;

public class registroPlanilha {
    // Campos originais (como lidos da planilha)
    private String matricula;
    private String cpf;
    private String nome;
    private String nivelEstagio;
    private String dataInicioStr;
    private String dataFimStr;
    private String banco;
    private String agencia;
    private String conta;

    // Campos normalizados (para comparação)
    private String matriculaNorm;
    private String cpfNorm;
    private String nomeNorm;
    private String nivelEstagioNorm;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String bancoNorm;
    private String agenciaNorm;
    private String contaNorm;

    // Construtor vazio
    public registroPlanilha() {}

    // Getters e Setters
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getNivelEstagio() { return nivelEstagio; }
    public void setNivelEstagio(String nivelEstagio) { this.nivelEstagio = nivelEstagio; }

    public String getDataInicioStr() { return dataInicioStr; }
    public void setDataInicioStr(String dataInicioStr) { this.dataInicioStr = dataInicioStr; }

    public String getDataFimStr() { return dataFimStr; }
    public void setDataFimStr(String dataFimStr) { this.dataFimStr = dataFimStr; }

    public String getBanco() { return banco; }
    public void setBanco(String banco) { this.banco = banco; }

    public String getAgencia() { return agencia; }
    public void setAgencia(String agencia) { this.agencia = agencia; }

    public String getConta() { return conta; }
    public void setConta(String conta) { this.conta = conta; }

    public String getMatriculaNorm() { return matriculaNorm; }
    public void setMatriculaNorm(String matriculaNorm) { this.matriculaNorm = matriculaNorm; }

    public String getCpfNorm() { return cpfNorm; }
    public void setCpfNorm(String cpfNorm) { this.cpfNorm = cpfNorm; }

    public String getNomeNorm() { return nomeNorm; }
    public void setNomeNorm(String nomeNorm) { this.nomeNorm = nomeNorm; }

    public String getNivelEstagioNorm() { return nivelEstagioNorm; }
    public void setNivelEstagioNorm(String nivelEstagioNorm) { this.nivelEstagioNorm = nivelEstagioNorm; }

    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }

    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }

    public String getBancoNorm() { return bancoNorm; }
    public void setBancoNorm(String bancoNorm) { this.bancoNorm = bancoNorm; }

    public String getAgenciaNorm() { return agenciaNorm; }
    public void setAgenciaNorm(String agenciaNorm) { this.agenciaNorm = agenciaNorm; }

    public String getContaNorm() { return contaNorm; }
    public void setContaNorm(String contaNorm) { this.contaNorm = contaNorm; }
}