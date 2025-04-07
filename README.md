# Projeto de Restaurante ( JavaSpring + MySQL)

Este projeto tem como objetivo desenvolver um sistema para gerenciamento de pedidos em um restaurante, utilizando JavaSpring para a lógica de negócio e MySQL para o armazenamento de dados. A interface gráfica será implementada com Java Swing, simulando um tablet para efetuar pedidos.

---

## ✨ Funcionalidade Principal

- Pedidos de Hambúrgueres, Bebidas e Sobremesas
- Cardápio com filtros para facilitar a escolha dos clientes.
- Exibição de fotos dos itens no cardápio.
- Pagamento via aplicativo.
- Armazenamento de Dados em MySQL
- Interface Gráfica com Java Swing

---

## 🧱 Construção do Projeto

> 🔹 Parte Restaurente
- Interface de Menu com as opções:
  - Ver Pedidos (Com o tempo que o produto está aguardando)
  - Estoque (Unidades que possuem cada item que esta sendo ofertado)
  - Adicionar Produtos
  - Ver ganho total do dia (Aparecendo todos pagamentos realizados pelos clientes)
> 🔹 Parte Cliente
- Interface de Login usuario
  - Apos efetuar o login com CPF cliente podera escolher entre a mesa (1 - 3) ou entrar em uma fila de espera 
- Tela principal do Tablet onde conseguira escolher entre Pagamento e Cardapio
- Cardapio onde vai apresentar todos itens que estão a venda com a foto dos itens ao lado
  - Filtro para decidir entre Bebida, Hamburger e Sobremesas
- Pagamento onde vai aparecer ao cliente os produtos que foram consumidos na mesa onde estava
---

## 🧑‍💻 Diagramas

### Diagrama de Classe

![Diagrama de Classe](https://github.com/Iago-Vargas/ProjetoRestaurante/blob/main/Diagramas/Diagrama%20de%20Classe.png)

O diagrama completo reúne todas as classes do sistema entidades, DAOs, conexão e interface gráfica em uma única visão integrada. Ele apresenta todas as variáveis, métodos e relacionamentos entre os módulos, permitindo compreender de forma ampla e detalhada como os componentes se comunicam entre si. Esse diagrama é ideal para ter uma visão macro do projeto e entender a arquitetura geral da aplicação.
![Diagrama de Classe Entidades](https://github.com/Iago-Vargas/ProjetoRestaurante/blob/main/Diagramas/Diagrama%20de%20Classe%20Entidades.png)

Este diagrama representa as entidades centrais do sistema, como Comida, Bebida, Sobremesa, Mesa e Pedido. Ele mostra a estrutura completa de cada classe, incluindo todos os atributos, construtores e métodos públicos disponíveis. Essas classes são responsáveis por representar os dados manipulados ao longo do sistema, como os itens do cardápio e os pedidos feitos pelos clientes.
![Diagrama de Classe DAO e Conexao](https://github.com/Iago-Vargas/ProjetoRestaurante/blob/main/Diagramas/Diagrama%20de%20Classe%20DAO%20e%20conexao.png)

O segundo diagrama ilustra a camada de acesso a dados, com destaque para as classes RestauranteDAO, CozinhaDAO e Conexao. Ele detalha como o sistema interage com o banco de dados, apresentando todos os métodos utilizados para realizar operações como listar, inserir, atualizar e excluir dados. A classe Conexao é responsável por estabelecer a comunicação com o banco, sendo usada pelas classes DAO.

![Diagrama de Classe Interface](https://github.com/Iago-Vargas/ProjetoRestaurante/blob/main/Diagramas/Diagrama%20de%20Classe%20Interface.png)

Este diagrama mostra as principais telas e formulários do sistema, como Tablet, Cardapio, Pagamento, CadastroProdutos, Menu, entre outras. Cada uma dessas classes representa uma parte da interface com o usuário e contém os métodos que controlam os eventos da aplicação, como cliques em botões, carregamento de dados e navegação entre telas. Ele ajuda a visualizar como a interface interage com o restante da aplicação.

### Diagrama de Sequência
![Diagrama de Sequencia](https://github.com/Iago-Vargas/ProjetoRestaurante/blob/main/Diagramas/Diagrama%20de%20Sequencia.png)

Este diagrama de sequência descreve de forma detalhada o fluxo completo de um pedido realizado pelo cliente através do tablet até a finalização do pagamento. Ele ilustra as interações entre as classes Tablet, RestauranteDAO, Pedido e Pagamento, mostrando todas as chamadas de métodos, criação de objetos, manipulação de listas de pedidos e cálculo do valor total. O diagrama destaca a comunicação entre as camadas do sistema (interface, lógica e dados) e é fundamental para entender a dinâmica da aplicação durante a realização de um pedido no restaurante.


### Diagrama de Caso de Uso
![Diagrama de caso de uso](https://github.com/Iago-Vargas/ProjetoRestaurante/blob/main/Diagramas/Diagrama%20de%20Caso%20de%20Uso%20-%20Restaurante.png)


## 👤 Autores

- **Iago Vargas de Oliveira - github.com/Iago-Vargas**
- **Bruno Difante - github.com/bouulzzz**
- **Gabriel Teixeira - github.com/Teizinn**
> Curso de Ciência da Computação – Universidade Franciscana (UFN)

📧 E-mail: me@iagovargas.com & [iagovargas.com](https://iagovargas.com)
🔗 GitHub: [@IagoVargas](https://github.com/Iago-Vargas)  
