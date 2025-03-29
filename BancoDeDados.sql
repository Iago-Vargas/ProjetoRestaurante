-- Cria o banco de dados
CREATE DATABASE restaurante;
USE restaurante;

-- Tabela de mesas
CREATE TABLE mesas (
    id_mesa INT PRIMARY KEY AUTO_INCREMENT,
    numero INT NOT NULL UNIQUE,
    status ENUM('livre', 'ocupada') DEFAULT 'livre'
);

-- Tabela de cardápio
CREATE TABLE cardapio (
    id_item INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    preco DECIMAL(10, 2) NOT NULL,
    categoria ENUM('comida', 'bebida', 'sobremesa')
);

-- Tabela de pedidos
CREATE TABLE pedidos (
    id_pedido INT PRIMARY KEY AUTO_INCREMENT,
    id_mesa INT NOT NULL,
    id_item INT NOT NULL,
    quantidade INT DEFAULT 1,
    status ENUM('pendente', 'preparando', 'pronto') DEFAULT 'pendente',
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_mesa) REFERENCES mesas(id_mesa),
    FOREIGN KEY (id_item) REFERENCES cardapio(id_item)
);

-- Insere dados iniciais (exemplo)
INSERT INTO mesas (numero, status) VALUES 
(1, 'livre'), (2, 'livre'), (3, 'livre');

INSERT INTO cardapio (nome, preco, categoria) VALUES
('Hambúrguer Quatro Queijos', 25.90, 'comida'),
('Hambúrguer Vegetariano', 45.50, 'comida'),
('Refrigerante', 8.00, 'bebida'),
('Sorvete', 12.00, 'sobremesa');
