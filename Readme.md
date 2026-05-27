# 📊 Comparador de Planilhas de Estágios

![Java](https://img.shields.io/badge/Java-21+-orange?logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?logo=java&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.9+-blue?logo=apachemaven&logoColor=white)
![Apache POI](https://img.shields.io/badge/Apache%20POI-5.2.5-red)
![License](https://img.shields.io/badge/License-MIT-green)
![Status](https://img.shields.io/badge/Status-Em%20desenvolvimento-yellow)

Sistema desenvolvido em **Java** para comparar dados de estagiários entre duas planilhas Excel (Financeiro vs Cadastro), identificar divergências, gerar relatórios e manter histórico de comparações. Possui interface gráfica (JavaFX) e versão console, com autenticação de usuários e filtros por período.

---

## 📌 Índice

- [Sobre o projeto](#-sobre-o-projeto)
- [Funcionalidades](#-funcionalidades)
- [Tecnologias utilizadas](#️-tecnologias-utilizadas)
- [Pré-requisitos](#-pré-requisitos)
- [Configuração e execução](#️-configuração-e-execução)
- [Estrutura do projeto](#-estrutura-do-projeto)
- [Como usar](#️-como-usar)
- [Contribuição](#-contribuição)
- [Licença](#-licença)

---

## 📖 Sobre o projeto

Este projeto surgiu da necessidade de reconciliar mensalmente os dados de estagiários entre o setor financeiro (planilha com contratos ativos) e o setor de cadastro (planilha com registros de estágio). O sistema lê duas planilhas Excel, normaliza os dados (CPF, datas, nomes, agências, etc.) e as compara usando uma chave composta (CPF + Matrícula).

**O que o sistema identifica:**

- Registros conformes (idênticos em ambas as planilhas)
- Registros faltantes no cadastro (presentes apenas no financeiro)
- Registros excedentes no cadastro (presentes apenas no cadastro)
- Divergências campo a campo (classificadas como ERRO ou AVISO)
- Possíveis abreviações nos nomes
- Registros cancelados (quando a data de início contém "CANCELADO")
- Conflitos de CPF com matrícula diferente

Além disso, gera um relatório Excel detalhado com várias abas e permite autenticação de usuários via arquivo `.properties`.

---

## ✨ Funcionalidades

- **Leitura flexível de Excel** — detecta cabeçalhos por sinônimos (ignora maiúsculas/minúsculas, acentos e pontuação). Suporta qualquer ordem de colunas e colunas extras.

- **Normalização inteligente**
  - CPF: remove pontos/traços e completa com zeros à esquerda (11 dígitos)
  - Agência/Conta: padroniza para 5 dígitos (com zeros à esquerda)
  - Nomes: remove acentos, converte para minúsculo, compara similaridade (distância de Levenshtein)
  - Datas: aceita múltiplos formatos (`dd/MM/yyyy`, número serial do Excel, etc.)

- **Comparação avançada**
  - Chave composta: `CPF normalizado + "|" + Matrícula normalizada`
  - Similaridade de nomes com limiar configurável (85%)
  - Detecção de erro de digitação (diferença de 1–2 caracteres) vs abreviação
  - Campos comparados: matrícula, CPF, nome, nível de estágio, datas, banco, agência e conta

- **Interface gráfica (JavaFX)**
  - Tela de login com autenticação BCrypt
  - Seleção de arquivos via `FileChooser`
  - Barra de progresso e thread separada para não travar a UI
  - Exibição de resultados em área de texto com formatação

- **Relatório Excel (Apache POI)**
  - Abas: Resumo, Faltantes, Excedentes, Divergências, Conflitos, Possíveis Abreviações, Conformes, Cancelados e Detalhado
  - Cores condicionais (vermelho para ERRO, amarelo para AVISO)
  - Escolha do nome do arquivo e pasta de destino

- **Filtro por período** — filtra registros do financeiro por data de início mínima (ex: contratos a partir de maio/2025)

- **Autenticação de usuários** — credenciais armazenadas em `usuarios.properties` com hashes BCrypt, sem necessidade de banco de dados

- **Executável autônomo** — gera um `.exe` com `jpackage` incluindo JRE personalizada; roda em qualquer Windows sem instalar Java

---

## 🛠️ Tecnologias utilizadas

| Componente | Tecnologia | Finalidade |
|---|---|---|
| Linguagem | Java 21 | Lógica principal |
| Build | Maven 3.9+ | Gerenciamento de dependências e build |
| Interface | JavaFX 21 | Interface gráfica |
| Leitura de Excel | Apache POI 5.2.5 | Extração de dados de `.xlsx` / `.xls` |
| Normalização | `java.text.Normalizer` | Remoção de acentos |
| Similaridade | Levenshtein (implementada) | Comparação fuzzy de nomes |
| Hashing de senhas | jBCrypt 0.4 | Autenticação segura |
| Logs | Log4j2 2.21.1 | Logs internos do Apache POI |
| Empacotamento | jpackage (JDK 21) | Gerador de executável nativo |

---

## 📋 Pré-requisitos

**Para desenvolvimento** (executar via Maven):
- JDK 21 instalado e configurado (`JAVA_HOME`)
- Maven 3.9+ (ou use o wrapper `mvnw`)
- Git (opcional, para clonar)

**Para uso do executável** (`.exe`):
- Nenhum — o executável é autônomo.

---

## ⚙️ Configuração e execução

### 1. Clonar o repositório

```bash
git clone https://github.com/DouglasLira-Dev/planilha-reconciliation-java.git
cd planilha-reconciliation-java
```

### 2. Compilar com Maven

```bash
mvn clean compile
```

### 3. Executar a interface gráfica

```bash
mvn javafx:run
```

### 4. Executar a versão console

```bash
mvn exec:java
```

### 5. Executar via script (Windows)

```bash
# Interface gráfica
Comparador.bat

# Versão console
executar.bat
```

### 6. Gerar um executável nativo (.exe)

Baixe o **JavaFX SDK 21** em [gluonhq.com/products/javafx](https://gluonhq.com/products/javafx/) e extraia.  
Copie todos os `.jmod` da pasta `lib` para uma pasta separada (ex: `C:\javafx-jmods-21`).

```bash
mvn clean package

jpackage --input target \
  --name ComparadorEstagios \
  --main-jar comparador-estagios-1.0-SNAPSHOT.jar \
  --main-class br.com.projeto.comparador.interfaceApp \
  --module-path "C:\javafx-jmods-21" \
  --add-modules javafx.controls,javafx.fxml \
  --type app-image \
  --win-console
```

A pasta `ComparadorEstagios` será criada com o `.exe`. Compacte-a para distribuição.  
> Uma versão já compilada está disponível em [`ComparadorEstagios.rar`](ComparadorEstagios.rar).

---

## 📁 Estrutura do projeto

```
planilha-reconciliation-java/
├── pom.xml
├── dependency-reduced-pom.xml
├── Readme.md
├── LICENSE
├── .gitignore
├── Comparador.bat
├── executar.bat
├── ComparadorEstagios.rar
├── ComparadorEstagios/
└── src/
    └── main/
        ├── java/br/com/projeto/comparador/
        │   ├── interfaceApp.java
        │   ├── app.java
        │   ├── comparadorPlanilhas.java
        │   ├── geradorRelatorioExcel.java
        │   ├── model/
        │   │   └── registroPlanilha.java
        │   ├── reader/
        │   │   └── leitorPlanilha.java
        │   ├── util/
        │   │   └── normalizacao.java
        │   ├── service/
        │   │   └── AuthService.java
        │   └── database/
        └── resources/
            └── usuarios.properties
```

---

## 🖥️ Como usar

### Login

Na primeira execução, defina as credenciais no arquivo `usuarios.properties`.  
Formato: `usuario = $2a$10$...` (hash BCrypt da senha).

### Selecionar planilhas

Clique em **Buscar** ao lado de cada campo e escolha os arquivos `.xlsx` ou `.xls`. A ordem das colunas não importa — o sistema lê por cabeçalho e aceita sinônimos como `"Prontuario"` para matrícula ou `"DT ADMISSAO"` para data de início.

### Filtro por período

Após carregar o financeiro, informe um mês/ano mínimo (ex: `05/2025`) para filtrar apenas contratos com `dataInicio >= primeiro dia do mês`.

### Resultados e relatório Excel

Após a comparação, um resumo é exibido na tela. Ao gerar o relatório Excel, escolha nome e pasta de destino. O arquivo conterá as seguintes abas:

| Aba | Conteúdo |
|-----|----------|
| 01 - Resumo | Estatísticas gerais, datas e limiar de similaridade |
| 02 - Faltantes no Cadastro | Registros do financeiro não encontrados no cadastro |
| 03 - Excedentes no Cadastro | Registros do cadastro não encontrados no financeiro |
| 04 - Divergências | Campos diferentes (vermelho = erro, amarelo = aviso) |
| 05 - Conflitos CPF e Matrícula | Mesmo CPF com matrícula diferente |
| 06 - Possíveis Abreviações | Nomes com alta similaridade, possíveis abreviações |
| 07 - Registros Conformes | Registros idênticos nas duas planilhas |
| 08 - Cancelados no Cadastro | Registros cuja data de início contém "CANCELADO" |
| 09 - Comparação Detalhada | *(em desenvolvimento)* |

---

## 🤝 Contribuição

Contribuições são bem-vindas! Sinta-se à vontade para abrir *issues* ou enviar *pull requests*.  
Antes de propor mudanças, verifique se o código segue as boas práticas Java e a estrutura existente do projeto.

---

## 📄 Licença

Distribuído sob a licença MIT. Consulte o arquivo [LICENSE](LICENSE) para mais informações.

---

Desenvolvido por **Douglas Lira** — estudante de Análise e Desenvolvimento de Sistemas, em constante evolução. 🚀