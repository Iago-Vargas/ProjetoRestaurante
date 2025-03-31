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

-- Tabela de ingredientes
CREATE TABLE IF NOT EXISTS ingredientes (
    id_ingrediente INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(50) NOT NULL,
    quantidade DECIMAL(10,3) NOT NULL DEFAULT 0,
    unidade_medida VARCHAR(20) NOT NULL,
    estoque_minimo DECIMAL(10,3) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabela de cardápio (ATUALIZADA com coluna quantidade)
CREATE TABLE IF NOT EXISTS cardapio (
    id_item INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    preco DECIMAL(10, 2) NOT NULL CHECK (preco > 0),
    categoria ENUM('comida', 'bebida', 'sobremesa') NOT NULL,
    quantidade INT NOT NULL DEFAULT 0,  -- COLUNA ADICIONADA
    disponivel BOOLEAN DEFAULT TRUE,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabela de relação entre itens e ingredientes
CREATE TABLE IF NOT EXISTS item_ingredientes (
    id_item INT NOT NULL,
    id_ingrediente INT NOT NULL,
    quantidade_necessaria DECIMAL(10,3) NOT NULL,
    PRIMARY KEY (id_item, id_ingrediente),
    FOREIGN KEY (id_item) REFERENCES cardapio(id_item) ON DELETE CASCADE,
    FOREIGN KEY (id_ingrediente) REFERENCES ingredientes(id_ingrediente) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabela de pedidos
CREATE TABLE IF NOT EXISTS pedidos (
    id_pedido INT PRIMARY KEY AUTO_INCREMENT,
    id_mesa INT NOT NULL,
    id_item INT NOT NULL,
    quantidade INT DEFAULT 1 CHECK (quantidade > 0),
    status ENUM('pendente', 'preparando', 'pronto', 'entregue') DEFAULT 'pendente',
    observacoes TEXT,
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_mesa) REFERENCES mesas(id_mesa) ON DELETE CASCADE,
    FOREIGN KEY (id_item) REFERENCES cardapio(id_item) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Índices para melhor performance
CREATE INDEX idx_pedidos_status ON pedidos(status);
CREATE INDEX idx_pedidos_mesa ON pedidos(id_mesa);
CREATE INDEX idx_cardapio_categoria ON cardapio(categoria);
CREATE INDEX idx_item_ingredientes ON item_ingredientes(id_item);

-- Inserção de dados iniciais

-- Mesas
INSERT INTO mesas (numero, status) VALUES 
(1, 'livre'), (2, 'livre'), (3, 'livre'), (4, 'livre'), (5, 'livre');

-- Ingredientes
INSERT INTO ingredientes (nome, quantidade, unidade_medida, estoque_minimo) VALUES
('Pão de hambúrguer', 100, 'unidades', 20),
('Carne bovina', 50, 'kg', 10),
('Queijo', 80, 'fatias', 15),
('Refrigerante', 200, 'litros', 30),
('Leite', 30, 'litros', 5),
('Açúcar', 20, 'kg', 2),
('Sorvete', 15, 'litros', 3),
('Hambúrguer de carne', 60, 'unidades', 10),
('Alface', 10, 'unidades', 2),
('Tomate', 15, 'kg', 3),
('Pão doce', 40, 'unidades', 5),
('Chocolate', 25, 'kg', 5);

-- Cardápio (ATUALIZADO com quantidades)
INSERT INTO cardapio (nome, preco, categoria, quantidade) VALUES
('Hambúrguer Clássico', 25.90, 'comida', 50),
('Hambúrguer Vegetariano', 45.50, 'comida', 30),
('Pizza Margherita', 38.90, 'comida', 20),
('Coca-Cola 350ml', 8.00, 'bebida', 100),
('Suco Natural 500ml', 10.50, 'bebida', 80),
('Guaraná Jesus 350ml', 7.50, 'bebida', 75),
('Sorvete 2 bolas', 12.00, 'sobremesa', 60),
('Mousse de Chocolate', 15.90, 'sobremesa', 40),
('Pudim de Leite', 10.99, 'sobremesa', 35);

-- Relacionamento itens-ingredientes
-- Hambúrguer Clássico
INSERT INTO item_ingredientes VALUES 
(1, 1, 1),   -- 1 pão
(1, 8, 1),   -- 1 hambúrguer de carne
(1, 3, 2),   -- 2 fatias de queijo
(1, 9, 0.1), -- 0.1 unidade de alface
(1, 10, 0.1);-- 0.1kg de tomate

-- Hambúrguer Vegetariano
INSERT INTO item_ingredientes VALUES
(2, 1, 1),
(2, 3, 2),
(2, 9, 0.2),
(2, 10, 0.15);

-- Pizza Margherita
INSERT INTO item_ingredientes VALUES
(3, 3, 5),   -- 5 fatias de queijo
(3, 10, 0.3);-- 0.3kg de tomate

-- Bebidas
INSERT INTO item_ingredientes VALUES
(4, 4, 0.35),  -- Coca-Cola 350ml
(5, 5, 0.5),   -- Suco Natural 500ml (usa leite)
(6, 4, 0.35);  -- Guaraná Jesus 350ml

-- Sobremesas
INSERT INTO item_ingredientes VALUES
(7, 5, 0.2),   -- Sorvete 2 bolas (0.2L leite)
(7, 6, 0.05),  -- 0.05kg açúcar
(7, 7, 0.3),   -- 0.3L sorvete
(8, 5, 0.15),  -- Mousse (0.15L leite)
(8, 6, 0.1),   -- 0.1kg açúcar
(8, 12, 0.2),  -- 0.2kg chocolate
(9, 5, 0.25),  -- Pudim (0.25L leite)
(9, 6, 0.15),  -- 0.15kg açúcar
(9, 11, 1);    -- 1 pão doce

-- Trigger para atualizar disponibilidade do item
DELIMITER //
CREATE TRIGGER atualizar_disponibilidade_ingrediente
AFTER UPDATE ON ingredientes
FOR EACH ROW
BEGIN
    -- Atualiza status dos itens do cardápio quando ingredientes ficam abaixo do mínimo
    UPDATE cardapio c
    JOIN item_ingredientes ii ON c.id_item = ii.id_item
    SET c.disponivel = CASE 
        WHEN (SELECT MIN(i.quantidade >= ii.quantidade_necessaria) 
              FROM ingredientes i
              JOIN item_ingredientes ii2 ON i.id_ingrediente = ii2.id_ingrediente
              WHERE ii2.id_item = c.id_item) = 1 THEN TRUE
        ELSE FALSE
    END
    WHERE ii.id_ingrediente = NEW.id_ingrediente;
END//
DELIMITER ;

-- View para visualização de estoque crítico
CREATE VIEW estoque_critico AS
SELECT i.nome AS ingrediente, 
       i.quantidade, 
       i.unidade_medida, 
       i.estoque_minimo,
       GROUP_CONCAT(DISTINCT c.nome SEPARATOR ', ') AS itens_afetados
FROM ingredientes i
JOIN item_ingredientes ii ON i.id_ingrediente = ii.id_ingrediente
JOIN cardapio c ON ii.id_item = c.id_item
WHERE i.quantidade < i.estoque_minimo * 1.5  -- 50% acima do mínimo
GROUP BY i.id_ingrediente;

-- Procedure para repor estoque
DELIMITER //
CREATE PROCEDURE repor_estoque(IN id_ingred INT, IN qtd DECIMAL(10,3))
BEGIN
    START TRANSACTION;
    UPDATE ingredientes 
    SET quantidade = quantidade + qtd 
    WHERE id_ingrediente = id_ingred;
    COMMIT;
END//
DELIMITER ;
