-- Criação do banco de dados
CREATE DATABASE IF NOT EXISTS restaurante;
USE restaurante;

-- Tabela de mesas
CREATE TABLE IF NOT EXISTS mesas (
    id_mesa INT PRIMARY KEY AUTO_INCREMENT,
    numero INT NOT NULL UNIQUE,
    status ENUM('livre', 'ocupada') DEFAULT 'livre',
    data_ultima_ocupacao TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabela de cardápio simplificada
CREATE TABLE IF NOT EXISTS cardapio (
    id_item INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    preco DECIMAL(10, 2) NOT NULL CHECK (preco > 0),
    categoria ENUM('comida', 'bebida', 'sobremesa') NOT NULL,
    quantidade INT NOT NULL DEFAULT 0,
    disponivel BOOLEAN NOT NULL DEFAULT TRUE,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabela de pedidos
CREATE TABLE IF NOT EXISTS pedidos (
    id_pedido INT PRIMARY KEY AUTO_INCREMENT,
    id_mesa INT NOT NULL,
    id_item INT NOT NULL,
    quantidade INT DEFAULT 1 CHECK (quantidade > 0),
    status ENUM('preparando','pronto','entregue','cancelado') DEFAULT 'preparando',
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    observacoes TEXT,
    FOREIGN KEY (id_mesa) REFERENCES mesas(id_mesa),
    FOREIGN KEY (id_item) REFERENCES cardapio(id_item)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Índices para melhor performance
CREATE INDEX idx_pedidos_status ON pedidos(status);
CREATE INDEX idx_pedidos_mesa ON pedidos(id_mesa);
CREATE INDEX idx_cardapio_categoria ON cardapio(categoria);

-- Trigger para autodesabilitar produtos com estoque zero
DELIMITER //
CREATE TRIGGER autodesabilita_produto
BEFORE UPDATE ON cardapio
FOR EACH ROW
BEGIN
    IF NEW.quantidade <= 0 THEN
        SET NEW.disponivel = FALSE;
    END IF;
END//
DELIMITER ;

-- Trigger para atualizar status da mesa quando um pedido é feito
DELIMITER //
CREATE TRIGGER atualiza_status_mesa
AFTER INSERT ON pedidos
FOR EACH ROW
BEGIN
    UPDATE mesas SET status = 'ocupada' WHERE id_mesa = NEW.id_mesa;
END//
DELIMITER ;

-- Procedure para reativar produto com estoque
DELIMITER //
CREATE PROCEDURE reativar_produto(IN produto_id INT)
BEGIN
    DECLARE qtd_estoque INT;
    
    SELECT quantidade INTO qtd_estoque 
    FROM cardapio WHERE id_item = produto_id;
    
    IF qtd_estoque > 0 THEN
        UPDATE cardapio SET disponivel = TRUE WHERE id_item = produto_id;
        SELECT 1 AS success, 'Produto reativado com sucesso' AS message;
    ELSE
        SELECT 0 AS success, 'Não é possível reativar produto com estoque zero' AS message;
    END IF;
END//
DELIMITER ;

-- Inserção de dados iniciais
INSERT INTO mesas (numero, status) VALUES 
(1, 'livre'), (2, 'livre'), (3, 'livre'), (4, 'livre'), (5, 'livre');

INSERT INTO cardapio (nome, preco, categoria, quantidade, disponivel) VALUES
('Hambúrguer Clássico', 25.90, 'comida', 50, TRUE),
('Hambúrguer Vegetariano', 45.50, 'comida', 30, TRUE),
('Pizza Margherita', 38.90, 'comida', 20, TRUE),
('Coca-Cola 350ml', 8.00, 'bebida', 100, TRUE),
('Suco Natural 500ml', 10.50, 'bebida', 80, TRUE),
('Guaraná Jesus 350ml', 7.50, 'bebida', 75, TRUE),
('Sorvete 2 bolas', 12.00, 'sobremesa', 60, TRUE),
('Mousse de Chocolate', 15.90, 'sobremesa', 40, TRUE),
('Pudim de Leite', 10.99, 'sobremesa', 35, TRUE);
