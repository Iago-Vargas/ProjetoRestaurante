-- Cria o banco de dados (se já não existir)
CREATE DATABASE IF NOT EXISTS restaurante;
USE restaurante;

-- Tabela de mesas
CREATE TABLE IF NOT EXISTS mesas (
    id_mesa INT PRIMARY KEY AUTO_INCREMENT,
    numero INT NOT NULL UNIQUE,
    status ENUM('livre', 'ocupada') DEFAULT 'livre'
);

-- Tabela de cardápio (unificada)
CREATE TABLE IF NOT EXISTS cardapio (
    id_item INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    preco DECIMAL(10, 2) NOT NULL,
    categoria ENUM('comida', 'bebida', 'sobremesa')
);

-- Tabela de pedidos
CREATE TABLE IF NOT EXISTS pedidos (
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
INSERT IGNORE INTO mesas (numero, status) VALUES 
(1, 'livre'), (2, 'livre'), (3, 'livre');

-- Limpa dados antigos e insere novos
DELETE FROM cardapio;
INSERT INTO cardapio (nome, preco, categoria) VALUES
    ('Hambúrguer Quatro Queijos', 25.90, 'comida'),
    ('Hambúrguer Vegetariano', 45.50, 'comida'),
    ('Pizza Margherita', 38.90, 'comida'),
    ('Coca-Cola', 8.00, 'bebida'),
    ('Suco Natural', 10.50, 'bebida'),
    ('Guaraná Jesus', 7.50, 'bebida'),
    ('Sorvete', 12.00, 'sobremesa'),
    ('Mousse de Chocolate', 15.90, 'sobremesa'),
    ('Pudim', 10.99, 'sobremesa');
