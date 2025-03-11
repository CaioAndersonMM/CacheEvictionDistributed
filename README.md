# Simulação do Projeto num Sistema Cliente/Servidor

Este projeto simula um sistema cliente/servidor para gerenciamento de ordens de serviço (OS) com uma política de cache eviction baseada em FIFO.

## 📌 Introdução

O cache eviction é o processo de remoção de itens do cache quando ele atinge sua capacidade máxima, permitindo a inserção de novos dados. Nesta simulação, utilizamos a política **FIFO (First In, First Out)**, onde os itens mais antigos são removidos primeiro.

## 🚀 Funcionalidades

- **Cliente:**  
  - Busca ordens de serviço na base de dados.  
  - Cadastra novas ordens de serviço.  
  - Lista todas as ordens de serviço.  
  - Altera e remove ordens de serviço.  
  - Consulta a quantidade de registros.  

- **Servidor:**  
  - Mantém uma cache de tamanho fixo (30 elementos) usando a política FIFO.  
  - Gerencia uma base de dados implementada como **Árvore Balanceada, Tabelas Hash ou SGBD**.  
  - Responde às requisições do cliente.  
  - Registra operações em um log e exibe os itens na cache após cada operação.  

## 🛠 Tecnologias Utilizadas

- Linguagem: **Java**  
- Estruturas de dados: **Tabelas Hash, Lista Autoajustável**  
- Comunicação Cliente/Servidor: **Sockets, RPC**  

## 🔧 Estrutura do Projeto

```bash
📂 Src
├── 📂 Client
│   ├── Cliente.java
│   └── Clienteimp.java  # Implementação do cliente
├── 📂 Database
│   ├── TabelaHash, Cache, No, Lista e etc.
├── 📂 Server
│   ├── 📂 Impl # Implementações
│   ├── Servidores.java
├─── log.log
├─── autenticacao.txt
```

## 📚 Políticas de Cache Eviction
- **FIFO (First In, First Out)**: Remove o item mais antigo do cache.  

## 📌 Aplicações
Cache eviction é utilizado em diversos cenários, incluindo:  
- **Sistemas Operacionais:** Gestão de memória virtual, caches de disco.  
- **Navegadores Web:** Cache de páginas e scripts.  
- **Bancos de Dados:** Armazenamento de consultas frequentes.  
- **Redes de Distribuição de Conteúdo (CDNs):** Redução da latência no acesso a conteúdos.  

## 📝 Logs e Exibição da Cache

Cada operação do servidor será registrada em um arquivo `log.txt` e a política de cache eviction será demonstrada ao exibir os itens armazenados na cache após cada ação.

## 📜 Desenvolvedores

 **[Caio Anderson Martins Moura](https://github.com/)** 🚀
 **[Ben Ariel França Martins](https://github.com/)** 🚀
