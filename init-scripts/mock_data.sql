-- SQL Mock Data Generation Script
-- Target: PostgreSQL
-- Project: Demo Database Population
-- Version: 2.0 (Column names aligned with schema dump)

-- Deletando todos os dados das tabelas na ordem correta para evitar violações de restrições
DELETE FROM public.order_item;
DELETE FROM public.payment;
DELETE FROM public.storage;
DELETE FROM public.purchase_order_item;
DELETE FROM public.purchase_order;
DELETE FROM public.orders;
DELETE FROM public.shipping_area;
DELETE FROM public.shipping_provider;
DELETE FROM public.product;
DELETE FROM public.employee;

-- Resetando sequências para tabelas com IDs autoincrementais (se aplicável)
-- Nota: A tabela banana_and_cream_cheese_2025 não está sendo populada conforme solicitado.
ALTER SEQUENCE public.banana_and_cream_cheese_2025_id_seq RESTART WITH 1;


-- ##################################################################
-- ##                      Tabela employee                         ##
-- ##################################################################
-- Inserindo 15 funcionários com cargos, datas de nascimento e status variados.
INSERT INTO public.employee (id, active, birthdate, created_at, fullname, gender, role) VALUES
                                                                                            (gen_random_uuid(), true, '1988-05-12', NOW() - interval '3 year', 'Carlos Santana', 'MALE', 'LOCAL_MANAGER'),
                                                                                            (gen_random_uuid(), true, '1992-08-20', NOW() - interval '2 year', 'Fernanda Lima', 'FEMALE', 'FINANCIAL'),
                                                                                            (gen_random_uuid(), true, '1995-01-30', NOW() - interval '1 year', 'Ricardo Souza', 'MALE', 'HR'),
                                                                                            (gen_random_uuid(), true, '1998-11-10', NOW() - interval '6 month', 'Beatriz Costa', 'FEMALE', 'SAC'),
                                                                                            (gen_random_uuid(), true, '2000-03-25', NOW() - interval '2 month', 'Lucas Martins', 'MALE', 'STORAGE'),
                                                                                            (gen_random_uuid(), true, '1993-07-15', NOW() - interval '1 year', 'Juliana Alves', 'FEMALE', 'SHIPPING'),
                                                                                            (gen_random_uuid(), true, '1990-09-05', NOW() - interval '4 year', 'Roberto Pereira', 'MALE', 'SALES'),
                                                                                            (gen_random_uuid(), true, '1996-02-18', NOW() - interval '2 year', 'Aline Gomes', 'FEMALE', 'SALES'),
                                                                                            (gen_random_uuid(), true, '1994-06-22', NOW() - interval '3 year', 'Marcos Rodrigues', 'MALE', 'SALES'),
                                                                                            (gen_random_uuid(), true, '1999-04-12', NOW() - interval '1 year', 'Gabriela Ferreira', 'FEMALE', 'SALES'),
                                                                                            (gen_random_uuid(), true, '1985-12-01', NOW() - interval '5 year', 'Thiago Oliveira', 'MALE', 'SALES'),
                                                                                            (gen_random_uuid(), true, '2001-01-15', NOW() - interval '6 month', 'Larissa Santos', 'FEMALE', 'SALES'),
                                                                                            (gen_random_uuid(), true, '1997-10-08', NOW() - interval '2 year', 'João Silva', 'MALE', 'SALES'),
                                                                                            (gen_random_uuid(), false, '1989-03-14', NOW() - interval '5 year', 'Patrícia Andrade', 'FEMALE', 'STORAGE'),
                                                                                            (gen_random_uuid(), false, '1991-08-25', NOW() - interval '4 year', 'Daniel Ribeiro', 'NON_BINARY', 'SHIPPING');


-- ##################################################################
-- ##           Tabelas shipping_provider e shipping_area          ##
-- ##################################################################
-- Inserindo 5 transportadoras fictícias.
INSERT INTO public.shipping_provider (id, active, average_delivery_days, base_price, cnpj, created_at, daily_capacity, name) VALUES
                                                                                                                                 (gen_random_uuid(), true, 5, 15.50, '11.222.333/0001-44', NOW() - interval '3 year', 5000, 'RápidoLog'),
                                                                                                                                 (gen_random_uuid(), true, 3, 18.00, '22.333.444/0001-55', NOW() - interval '2 year', 8000, 'ExpressBrasil'),
                                                                                                                                 (gen_random_uuid(), true, 7, 12.75, '33.444.555/0001-66', NOW() - interval '5 year', 3000, 'LogiSul'),
                                                                                                                                 (gen_random_uuid(), true, 4, 14.00, '44.555.666/0001-77', NOW() - interval '1 year', 10000, 'TransNorte'),
                                                                                                                                 (gen_random_uuid(), false, 10, 10.50, '55.666.777/0001-88', NOW() - interval '6 year', 2000, 'CargaPesada');

-- Inserindo áreas de entrega e associando-as às transportadoras.
INSERT INTO public.shipping_area (id, active, created_at, description, states, shipping_provider_id) VALUES
    (gen_random_uuid(), true, NOW() - interval '2 year', 'Região Sudeste', 'SP,RJ,ES,MG', (SELECT id FROM public.shipping_provider WHERE name = 'RápidoLog' LIMIT 1)),
(gen_random_uuid(), true, NOW() - interval '1 year', 'Região Sul', 'PR,SC,RS', (SELECT id FROM public.shipping_provider WHERE name = 'ExpressBrasil' LIMIT 1)),
(gen_random_uuid(), true, NOW() - interval '4 year', 'Todo o Brasil', 'TODOS', (SELECT id FROM public.shipping_provider WHERE name = 'LogiSul' LIMIT 1)),
(gen_random_uuid(), true, NOW() - interval '6 months', 'Regiões Norte e Nordeste', 'AM,PA,MA,PI,CE,RN,PB,PE,AL,SE,BA', (SELECT id FROM public.shipping_provider WHERE name = 'TransNorte' LIMIT 1)),
(gen_random_uuid(), false, NOW() - interval '5 year', 'Apenas Cargas Especiais para o Sudeste', 'SP,RJ', (SELECT id FROM public.shipping_provider WHERE name = 'CargaPesada' LIMIT 1));


-- ##################################################################
-- ##                       Tabela product                         ##
-- ##################################################################
-- Mapeamento: mg:0, g:1, Kg:2, Ml:3, l:4, Bl:5, Cx:6, Un:7, Gt:8

-- Categoria: Analgésicos e Antitérmicos
INSERT INTO public.product (sku, active, created_at, description, measurementunit, name) VALUES
                                                                                             ('PROD001', true, NOW() - interval '2 year', 'Dipirona Sódica 500mg/mL, 20mL', 3, 'Dipirona Gotas'),
                                                                                             ('PROD002', true, NOW() - interval '2 year', 'Paracetamol 750mg, caixa com 20 comprimidos', 6, 'Paracetamol 750mg'),
                                                                                             ('PROD003', true, NOW() - interval '1 year', 'Ibuprofeno 400mg, caixa com 10 cápsulas', 6, 'Ibuprofeno 400mg'),
                                                                                             ('PROD004', true, NOW() - interval '3 year', 'Ácido Acetilsalicílico 100mg, caixa com 30 comprimidos', 6, 'AAS Infantil'),
                                                                                             ('PROD005', true, NOW() - interval '6 months', 'Dorflex, caixa com 36 comprimidos', 6, 'Dorflex'),
                                                                                             ('PROD006', true, NOW() - interval '1 year', 'Neosaldina, caixa com 20 drágeas', 6, 'Neosaldina');

-- Categoria: Vitaminas e Suplementos
INSERT INTO public.product (sku, active, created_at, description, measurementunit, name) VALUES
                                                                                             ('PROD010', true, NOW() - interval '1 year', 'Vitamina C 1g, tubo com 10 comprimidos efervescentes', 6, 'Vitamina C Efervescente'),
                                                                                             ('PROD011', true, NOW() - interval '1 year', 'Complexo B, caixa com 30 comprimidos', 6, 'Complexo B'),
                                                                                             ('PROD012', true, NOW() - interval '2 year', 'Suplemento de Vitamina D 2000UI, 30 cápsulas', 6, 'Vitamina D 2000UI'),
                                                                                             ('PROD013', true, NOW() - interval '6 months', 'Lavitan A-Z, caixa com 60 comprimidos', 6, 'Lavitan A-Z'),
                                                                                             ('PROD014', false, NOW() - interval '3 year', 'Ômega 3 1000mg, pote com 120 cápsulas', 6, 'Ômega 3');

-- Categoria: Dermocosméticos
INSERT INTO public.product (sku, active, created_at, description, measurementunit, name) VALUES
                                                                                             ('PROD020', true, NOW() - interval '1 year', 'Protetor Solar FPS 50, 120mL', 3, 'Protetor Solar Sundown FPS 50'),
                                                                                             ('PROD021', true, NOW() - interval '2 year', 'Creme Hidratante Nivea, lata 56g', 1, 'Creme Nivea'),
                                                                                             ('PROD022', true, NOW() - interval '8 months', 'Sabonete Líquido Facial Actine, 140mL', 3, 'Sabonete Actine'),
                                                                                             ('PROD023', true, NOW() - interval '1 year', 'Água Micelar L''Oréal Paris, 200mL', 3, 'Água Micelar L''Oréal'),
                                                                                             ('PROD024', true, NOW() - interval '3 year', 'Creme anti-idade Cicatricure, 30g', 1, 'Cicatricure Creme');

-- Categoria: Higiene Pessoal
INSERT INTO public.product (sku, active, created_at, description, measurementunit, name) VALUES
                                                                                             ('PROD030', true, NOW() - interval '2 year', 'Shampoo Pantene Restauração, 400mL', 3, 'Shampoo Pantene'),
                                                                                             ('PROD031', true, NOW() - interval '2 year', 'Condicionador Dove Óleo Nutrição, 200mL', 3, 'Condicionador Dove'),
                                                                                             ('PROD032', true, NOW() - interval '1 year', 'Pasta de Dente Colgate Total 12, 90g', 1, 'Colgate Total 12'),
                                                                                             ('PROD033', true, NOW() - interval '3 year', 'Fio Dental Oral-B, 50m', 7, 'Fio Dental Oral-B'),
                                                                                             ('PROD034', true, NOW() - interval '1 year', 'Desodorante Aerosol Rexona, 150mL', 3, 'Desodorante Rexona');

-- Categoria: Primeiros Socorros
INSERT INTO public.product (sku, active, created_at, description, measurementunit, name) VALUES
                                                                                             ('PROD040', true, NOW() - interval '4 year', 'Band-Aid, caixa com 40 unidades', 6, 'Band-Aid'),
                                                                                             ('PROD041', true, NOW() - interval '3 year', 'Mertiolate Antisséptico, 30mL', 3, 'Mertiolate'),
                                                                                             ('PROD042', true, NOW() - interval '2 year', 'Gaze Estéril, pacote com 10 unidades', 6, 'Gaze Estéril'),
                                                                                             ('PROD043', false, NOW() - interval '5 year', 'Água Oxigenada 10 volumes, 100mL', 3, 'Água Oxigenada');

-- Adicionando mais 76 produtos para atingir o total de 100
INSERT INTO public.product (sku, active, created_at, description, measurementunit, name) VALUES
                                                                                             ('PROD044', true, NOW() - interval '1 year', 'Esparadrapo Impermeável, rolo', 7, 'Esparadrapo'),
                                                                                             ('PROD045', true, NOW() - interval '2 year', 'Antisséptico Bucal Listerine, 500mL', 3, 'Listerine Cool Mint'),
                                                                                             ('PROD046', true, NOW() - interval '1 year', 'Cotonetes, caixa com 150 unidades', 6, 'Cotonetes Johnson''s'),
                                                                                             ('PROD047', true, NOW() - interval '3 year', 'Algodão em Bola, pacote 50g', 1, 'Algodão Apolo'),
                                                                                             ('PROD048', true, NOW() - interval '1 year', 'Soro Fisiológico, 500mL', 3, 'Soro Fisiológico'),
                                                                                             ('PROD049', true, NOW() - interval '6 months', 'Repelente de Insetos Exposis, 100mL', 3, 'Repelente Exposis'),
                                                                                             ('PROD050', true, NOW() - interval '2 year', 'Termômetro Digital G-Tech', 7, 'Termômetro Digital'),
                                                                                             ('PROD051', true, NOW() - interval '1 year', 'Xarope para Tosse Vick, 120mL', 3, 'Xarope Vick'),
                                                                                             ('PROD052', true, NOW() - interval '4 year', 'Pastilhas para Garganta Benalet, 12 unidades', 6, 'Pastilhas Benalet'),
                                                                                             ('PROD053', true, NOW() - interval '2 year', 'Antiácido Eno, sachê 5g', 1, 'Sal de Fruta Eno'),
                                                                                             ('PROD054', true, NOW() - interval '1 year', 'Antialérgico Loratadina 10mg, 12 comprimidos', 6, 'Loratadina'),
                                                                                             ('PROD055', true, NOW() - interval '3 year', 'Relaxante Muscular Torsilax, 30 comprimidos', 6, 'Torsilax'),
                                                                                             ('PROD056', true, NOW() - interval '1 year', 'Creme para Assaduras Hipoglós, 45g', 1, 'Hipoglós'),
                                                                                             ('PROD057', true, NOW() - interval '2 year', 'Fralda Descartável Pampers, pacote M', 6, 'Fralda Pampers M'),
                                                                                             ('PROD058', true, NOW() - interval '1 year', 'Lenços Umedecidos Huggies, 48 unidades', 6, 'Lenços Umedecidos Huggies'),
                                                                                             ('PROD059', true, NOW() - interval '6 months', 'Shampoo Infantil Johnson''s, 200mL', 3, 'Shampoo Johnson''s Baby'),
                                                                                             ('PROD060', true, NOW() - interval '1 year', 'Protetor Labial Nivea, 4.8g', 1, 'Protetor Labial Med Repair'),
                                                                                             ('PROD061', true, NOW() - interval '2 year', 'Gel para Cabelo Bozzano, 300g', 1, 'Gel Bozzano'),
                                                                                             ('PROD062', true, NOW() - interval '1 year', 'Creme de Barbear Gillette, 65g', 1, 'Creme de Barbear Gillette'),
                                                                                             ('PROD063', true, NOW() - interval '3 year', 'Loção Pós-Barba Nivea, 100mL', 3, 'Loção Pós-Barba Nivea'),
                                                                                             ('PROD064', true, NOW() - interval '1 year', 'Sabonete Íntimo Dermacyd, 200mL', 3, 'Sabonete Íntimo Dermacyd'),
                                                                                             ('PROD065', true, NOW() - interval '2 year', 'Absorvente Intimus Gel, pacote com 8', 6, 'Absorvente Intimus'),
                                                                                             ('PROD066', true, NOW() - interval '1 year', 'Tintura de Cabelo Koleston, kit', 6, 'Koleston Cor 6.7'),
                                                                                             ('PROD067', true, NOW() - interval '4 year', 'Adoçante Zero-Cal, 100mL', 3, 'Adoçante Zero-Cal'),
                                                                                             ('PROD068', true, NOW() - interval '2 year', 'Barra de Cereal Nutry, unidade', 7, 'Barra de Cereal Nutry'),
                                                                                             ('PROD069', true, NOW() - interval '1 year', 'Isotônico Gatorade, 500mL', 3, 'Gatorade Limão'),
                                                                                             ('PROD070', true, NOW() - interval '3 year', 'Teste de Gravidez Confirme, unidade', 7, 'Teste de Gravidez Confirme'),
                                                                                             ('PROD071', true, NOW() - interval '1 year', 'Preservativo Jontex, pacote com 3', 6, 'Preservativo Jontex'),
                                                                                             ('PROD072', true, NOW() - interval '2 year', 'Lubrificante Íntimo K-Y, 50g', 1, 'Lubrificante K-Y'),
                                                                                             ('PROD073', true, NOW() - interval '1 year', 'Analgésico Tylenol 500mg, 20 comprimidos', 6, 'Tylenol 500mg'),
                                                                                             ('PROD074', true, NOW() - interval '6 months', 'Advil 400mg, 8 cápsulas', 6, 'Advil'),
                                                                                             ('PROD075', true, NOW() - interval '1 year', 'Vitamina C Redoxon, 10 comprimidos', 6, 'Redoxon Gotas'),
                                                                                             ('PROD076', true, NOW() - interval '2 year', 'Cálcio Osteo-Bi-Flex, 60 tabletes', 6, 'Osteo-Bi-Flex'),
                                                                                             ('PROD077', true, NOW() - interval '1 year', 'Gel Massageador Doutorzinho, 120g', 1, 'Gel Doutorzinho'),
                                                                                             ('PROD078', true, NOW() - interval '3 year', 'Creme para Varizes Venalot, 60g', 1, 'Venalot Creme'),
                                                                                             ('PROD079', true, NOW() - interval '1 year', 'Salompas Adesivo, envelope com 2', 6, 'Salompas'),
                                                                                             ('PROD080', true, NOW() - interval '2 year', 'Nebulizador G-Tech, aparelho', 7, 'Nebulizador G-Tech'),
                                                                                             ('PROD081', true, NOW() - interval '1 year', 'Medidor de Pressão Digital Omron', 7, 'Medidor de Pressão Omron'),
                                                                                             ('PROD082', true, NOW() - interval '4 year', 'Engov, envelope com 6 comprimidos', 6, 'Engov'),
                                                                                             ('PROD083', true, NOW() - interval '2 year', 'Magnésia Bisurada, 20 pastilhas', 6, 'Magnésia Bisurada'),
                                                                                             ('PROD084', true, NOW() - interval '1 year', 'Azia e Má Digestão Estomazil, 5g', 1, 'Estomazil'),
                                                                                             ('PROD085', true, NOW() - interval '3 year', 'Colírio Moura Brasil, 20mL', 3, 'Colírio Moura Brasil'),
                                                                                             ('PROD086', true, NOW() - interval '1 year', 'Descongestionante Nasal Neosoro, 30mL', 3, 'Neosoro Adulto'),
                                                                                             ('PROD087', true, NOW() - interval '2 year', 'Creme Dental Sensodyne, 90g', 1, 'Sensodyne Branqueador'),
                                                                                             ('PROD088', true, NOW() - interval '1 year', 'Escova de Dente Oral-B Indicator', 7, 'Escova Oral-B'),
                                                                                             ('PROD089', true, NOW() - interval '6 months', 'Sabonete Protex Limpeza Profunda, 90g', 1, 'Sabonete Protex'),
                                                                                             ('PROD090', true, NOW() - interval '1 year', 'Acetona, 100mL', 3, 'Removedor de Esmalte'),
                                                                                             ('PROD091', true, NOW() - interval '2 year', 'Esmalte Risqué Renda, 8mL', 3, 'Esmalte Renda'),
                                                                                             ('PROD092', true, NOW() - interval '1 year', 'Talco para Pés Tenys Pé, 100g', 1, 'Tenys Pé Baruel'),
                                                                                             ('PROD093', true, NOW() - interval '3 year', 'Pomada para Assaduras Bepantol, 30g', 1, 'Bepantol Baby'),
                                                                                             ('PROD094', true, NOW() - interval '1 year', 'Fralda Geriátrica Bigfral, pacote M', 6, 'Fralda Bigfral'),
                                                                                             ('PROD095', true, NOW() - interval '2 year', 'Mamadeira Kuka, 240mL', 7, 'Mamadeira Kuka'),
                                                                                             ('PROD096', true, NOW() - interval '1 year', 'Chupeta Lillo, unidade', 7, 'Chupeta Lillo'),
                                                                                             ('PROD097', true, NOW() - interval '6 months', 'Leite em Pó Aptamil, 800g', 1, 'Fórmula Infantil Aptamil'),
                                                                                             ('PROD098', true, NOW() - interval '1 year', 'Cereal Infantil Mucilon, 400g', 1, 'Mucilon Arroz e Aveia'),
                                                                                             ('PROD099', true, NOW() - interval '2 year', 'Bico de Mamadeira de Silicone, tam 2', 7, 'Bico de Mamadeira Kuka'),
                                                                                             ('PROD100', true, NOW() - interval '1 year', 'Aparelho de Barbear Gillette Mach3', 7, 'Gillette Mach3'),
                                                                                             ('PROD101', true, NOW() - interval '3 year', 'Carga para Aparelho de Barbear, 2 un', 6, 'Carga Mach3'),
                                                                                             ('PROD102', true, NOW() - interval '1 year', 'Tira-Leite Manual Lillo', 7, 'Bomba Tira-Leite'),
                                                                                             ('PROD103', true, NOW() - interval '2 year', 'Almofada para Amamentação', 7, 'Almofada de Amamentação'),
                                                                                             ('PROD104', true, NOW() - interval '1 year', 'Meia de Compressão Venosan, par', 7, 'Meia de Compressão'),
                                                                                             ('PROD105', true, NOW() - interval '6 months', 'Joelheira Elástica, unidade', 7, 'Joelheira Needs'),
                                                                                             ('PROD106', true, NOW() - interval '1 year', 'Bolsa Térmica Gel, unidade', 7, 'Bolsa Térmica'),
                                                                                             ('PROD107', true, NOW() - interval '2 year', 'Melagrião Xarope, 150mL', 3, 'Melagrião Xarope'),
                                                                                             ('PROD108', true, NOW() - interval '1 year', 'Decongex Plus, 20mL gotas', 8, 'Decongex Gotas'),
                                                                                             ('PROD109', true, NOW() - interval '3 year', 'Allegra 120mg, 10 comprimidos', 6, 'Allegra'),
                                                                                             ('PROD110', true, NOW() - interval '1 year', 'Dramin B6, 10 comprimidos', 6, 'Dramin'),
                                                                                             ('PROD111', true, NOW() - interval '2 year', 'Plasil Gotas, 20mL', 8, 'Plasil Gotas'),
                                                                                             ('PROD112', true, NOW() - interval '1 year', 'Luftal Gotas, 15mL', 8, 'Luftal Gotas'),
                                                                                             ('PROD113', true, NOW() - interval '6 months', 'Imosec, 12 comprimidos', 6, 'Imosec'),
                                                                                             ('PROD114', true, NOW() - interval '1 year', 'Cataflampro Emulgel, 60g', 1, 'Cataflampro'),
                                                                                             ('PROD115', true, NOW() - interval '2 year', 'Gelo-Bio Aerossol, 120mL', 3, 'Gelo-Bio'),
                                                                                             ('PROD116', true, NOW() - interval '1 year', 'Caladryl Creme, 28g', 1, 'Caladryl'),
                                                                                             ('PROD117', true, NOW() - interval '3 year', 'Fungicid, 30mL', 3, 'Fungicid Solução'),
                                                                                             ('PROD118', true, NOW() - interval '1 year', 'Minancora, lata 30g', 1, 'Pomada Minancora'),
                                                                                             ('PROD119', true, NOW() - interval '2 year', 'Centrum Mulher, 30 comprimidos', 6, 'Centrum Mulher');


-- ##################################################################
-- ##                       Tabela storage                         ##
-- ##################################################################
-- Populando o estoque para cada produto criado.
-- A quantidade é um número aleatório entre 20 e 250.
-- Produtos inativos ou selecionados terão estoque 0.
INSERT INTO public.storage (id, product_sku, product_quantity)
SELECT gen_random_uuid(), sku,
       CASE
           WHEN not active THEN 0 -- Estoque zero para produtos inativos
           WHEN sku IN ('PROD001', 'PROD022') THEN 0 -- Simula ruptura de estoque para produtos ativos
           ELSE floor(random() * (250 - 20 + 1) + 20)::int
END
FROM public.product;

-- ##################################################################
-- ##                       Tabela payment                         ##
-- ##################################################################
-- Inserindo registros de pagamento para 7 funcionários de cargos variados.
-- Os valores seguem regras de negócio específicas para cada cargo.
-- O imposto (amount_in_taxes) é uma simulação progressiva.

-- LOCAL_MANAGER
INSERT INTO public.payment (id, active, amount_in_taxes, created_at, dental_insurance_amount, food_voucher_amount, gross_income, health_insurance_amount, meal_voucher_amount, profit_sharing_amount, employee_id) VALUES
    (gen_random_uuid(), true, 705.00, NOW() - interval '1 month', 50.00, 300.00, 6000.00, 400.00, 700.00, 1200.00, (SELECT id FROM public.employee WHERE role = 'LOCAL_MANAGER' LIMIT 1));

-- FINANCIAL
INSERT INTO public.payment (id, active, amount_in_taxes, created_at, dental_insurance_amount, food_voucher_amount, gross_income, health_insurance_amount, meal_voucher_amount, profit_sharing_amount, employee_id) VALUES
    (gen_random_uuid(), true, 227.00, NOW() - interval '1 month', 40.00, 250.00, 4000.00, 300.00, 500.00, 800.00, (SELECT id FROM public.employee WHERE role = 'FINANCIAL' LIMIT 1));

-- SALES (Exemplo 1)
INSERT INTO public.payment (id, active, amount_in_taxes, created_at, dental_insurance_amount, food_voucher_amount, gross_income, health_insurance_amount, meal_voucher_amount, profit_sharing_amount, employee_id) VALUES
    (gen_random_uuid(), true, 302.00, NOW() - interval '1 month', 45.00, 280.00, 4500.00, 350.00, 600.00, 900.00, (SELECT id FROM public.employee WHERE role = 'SALES' and fullname = 'Roberto Pereira' LIMIT 1));

-- SALES (Exemplo 2)
INSERT INTO public.payment (id, active, amount_in_taxes, created_at, dental_insurance_amount, food_voucher_amount, gross_income, health_insurance_amount, meal_voucher_amount, profit_sharing_amount, employee_id) VALUES
    (gen_random_uuid(), true, 302.00, NOW() - interval '1 month', 45.00, 280.00, 4500.00, 350.00, 600.00, 900.00, (SELECT id FROM public.employee WHERE role = 'SALES' and fullname = 'Aline Gomes' LIMIT 1));

-- HR
INSERT INTO public.payment (id, active, amount_in_taxes, created_at, dental_insurance_amount, food_voucher_amount, gross_income, health_insurance_amount, meal_voucher_amount, profit_sharing_amount, employee_id) VALUES
    (gen_random_uuid(), true, 140.00, NOW() - interval '1 month', 35.00, 200.00, 3500.00, 250.00, 450.00, 700.00, (SELECT id FROM public.employee WHERE role = 'HR' LIMIT 1));

-- SHIPPING
INSERT INTO public.payment (id, active, amount_in_taxes, created_at, dental_insurance_amount, food_voucher_amount, gross_income, health_insurance_amount, meal_voucher_amount, profit_sharing_amount, employee_id) VALUES
    (gen_random_uuid(), true, 35.00, NOW() - interval '1 month', 30.00, 180.00, 2500.00, 200.00, 350.00, 500.00, (SELECT id FROM public.employee WHERE role = 'SHIPPING' AND active = true LIMIT 1));

-- STORAGE
INSERT INTO public.payment (id, active, amount_in_taxes, created_at, dental_insurance_amount, food_voucher_amount, gross_income, health_insurance_amount, meal_voucher_amount, profit_sharing_amount, employee_id) VALUES
    (gen_random_uuid(), true, 0.00, NOW() - interval '1 month', 25.00, 150.00, 2000.00, 180.00, 300.00, 400.00, (SELECT id FROM public.employee WHERE role = 'STORAGE' AND active = true LIMIT 1));

-- Fim do Script

-- SQL Mock Data Generation Script - Part 2
-- Target: PostgreSQL
-- Project: Order History Population
-- Description: Creates a realistic history for orders, order_items, and shipping_orders.
--              This script is designed to be run AFTER the initial data population script.

DO $$
DECLARE
    -- ### CONFIGURAÇÃO ###
v_start_date DATE := '2025-04-01';
    v_end_date DATE := '2025-06-17';

    -- ### VARIÁVEIS DE CONTROLE ###
    v_current_date DATE;
    v_num_orders_per_day INT;
    v_num_items_per_order INT;
    v_created_at_timestamp TIMESTAMP;

    -- ### ARRAYS PARA ARMAZENAR DADOS EXISTENTES (Melhora a performance) ###
    v_seller_ids UUID[];
    v_product_skus VARCHAR(50)[];
    v_shipping_provider_ids UUID[];

    -- ### VARIÁVEIS PARA ARMAZENAR IDs E VALORES GERADOS ###
    v_order_id UUID;
    v_shipping_order_id UUID;
    v_seller_id UUID;
    v_product_sku VARCHAR(50);
    v_shipping_provider_id UUID;

    v_item_price NUMERIC(19,4);
    v_item_quantity INT;
    v_order_products_price NUMERIC(19,4);
    v_order_total_amount NUMERIC(19,4);

    v_shipping_cost NUMERIC(38,2);
    v_order_weight NUMERIC(38,2);
    v_shipment_date DATE;
    v_delivery_date DATE;
    v_estimated_days INT;

    v_order_status VARCHAR(255);
    v_shipping_status VARCHAR(255);

BEGIN
    -- 1. Carrega os IDs e SKUs necessários em arrays para acesso rápido, evitando queries repetidas no loop.
    RAISE NOTICE 'Carregando dados de apoio (vendedores, produtos, transportadoras)...';
    v_seller_ids := ARRAY(SELECT id FROM public.employee WHERE role = 'SALES' AND active = true);
    v_product_skus := ARRAY(SELECT sku FROM public.product WHERE active = true);
    v_shipping_provider_ids := ARRAY(SELECT id FROM public.shipping_provider WHERE active = true);

    -- Verifica se temos dados suficientes para continuar
    IF array_length(v_seller_ids, 1) IS NULL OR array_length(v_product_skus, 1) IS NULL OR array_length(v_shipping_provider_ids, 1) IS NULL THEN
        RAISE EXCEPTION 'Não há dados suficientes nas tabelas employee(SALES), product ou shipping_provider para gerar os pedidos. Execute o script inicial primeiro.';
END IF;

    RAISE NOTICE 'Iniciando a geração de pedidos de % até %...', v_start_date, v_end_date;

    -- 2. Loop principal: Itera por cada dia desde a data de início até a data de fim.
FOR v_current_date IN SELECT generate_series(v_start_date, v_end_date, '1 day'::interval) LOOP

                             -- Gera um número aleatório de pedidos para o dia corrente (entre 3 e 10)
                          v_num_orders_per_day := floor(random() * 8 + 3)::INT;

-- Loop secundário: Cria cada pedido para o dia corrente.
FOR i IN 1..v_num_orders_per_day LOOP

            -- Zera os totalizadores do pedido
            v_order_products_price := 0;
            v_order_total_amount := 0;
            v_order_weight := 0;

            -- Gera um timestamp aleatório para o dia corrente
            v_created_at_timestamp := v_current_date + (floor(random()*86400))::integer * '1 second'::interval;

            -- ### ETAPA A: Criar o `shipping_order` ###
            v_shipping_provider_id := v_shipping_provider_ids[floor(random() * array_length(v_shipping_provider_ids, 1) + 1)];
            v_shipping_cost := round((random() * (40 - 12) + 12)::numeric, 2);
            v_estimated_days := (SELECT average_delivery_days FROM public.shipping_provider WHERE id = v_shipping_provider_id LIMIT 1);
            v_shipment_date := v_current_date + interval '1 day';
            v_delivery_date := v_shipment_date + (v_estimated_days * interval '1 day');

            -- Lógica para status do frete
            IF v_delivery_date < v_end_date THEN
                v_shipping_status := 'ENTREGUE';
            ELSIF v_shipment_date < v_end_date THEN
                v_shipping_status := 'EM_TRANSPORTE';
ELSE
                v_shipping_status := 'PENDENTE';
END IF;

INSERT INTO public.shipping_order (id, active, created_at, delivery_date, destinationcity, destinationstate, estimated_delivery_days, shipment_date, shipping_cost, status, weight, shippingprovider_id)
VALUES (gen_random_uuid(), true, v_created_at_timestamp, v_delivery_date, 'Cidade Exemplo', 'SP', v_estimated_days, v_shipment_date, v_shipping_cost, v_shipping_status, 0, v_shipping_provider_id) -- Peso será atualizado depois
    RETURNING id INTO v_shipping_order_id;

-- ### ETAPA B: Criar o `orders` ###
v_seller_id := v_seller_ids[floor(random() * array_length(v_seller_ids, 1) + 1)];

            -- Lógica para status do pedido
            IF v_current_date < (v_end_date - interval '3 days') THEN
                v_order_status := 'INVOICED';
ELSE
                v_order_status := 'OPEN';
END IF;

INSERT INTO public.orders (id, created_at, description, orderstatus, products_price, total_amount, seller_id, shipping_order_id)
VALUES (gen_random_uuid(), v_created_at_timestamp, 'Pedido de venda gerado via script', v_order_status, 0, 0, v_seller_id, v_shipping_order_id) -- Preços serão atualizados depois
    RETURNING id INTO v_order_id;

-- ### ETAPA C: Criar os `order_item` (de 1 a 5 itens por pedido) ###
v_num_items_per_order := floor(random() * 5 + 1)::INT;
FOR j IN 1..v_num_items_per_order LOOP
                v_product_sku := v_product_skus[floor(random() * array_length(v_product_skus, 1) + 1)];
                v_item_price := round((random() * (250 - 10) + 10)::numeric, 4);
                v_item_quantity := floor(random() * 3 + 1)::INT;

INSERT INTO public.order_item (id, created_at, price, quantity, orders_id, product_sku)
VALUES (gen_random_uuid(), v_created_at_timestamp, v_item_price, v_item_quantity, v_order_id, v_product_sku);

-- Acumula o preço e o peso dos produtos
v_order_products_price := v_order_products_price + (v_item_price * v_item_quantity);
                v_order_weight := v_order_weight + (0.5 * v_item_quantity); -- Simula um peso de 0.5 kg por item
END LOOP;

            -- ### ETAPA D: Atualizar o `orders` e `shipping_order` com os totais calculados ###
            v_order_total_amount := v_order_products_price + v_shipping_cost;

UPDATE public.orders
SET products_price = round(v_order_products_price, 4),
    total_amount = round(v_order_total_amount, 4)
WHERE id = v_order_id;

UPDATE public.shipping_order
SET weight = round(v_order_weight, 2)
WHERE id = v_shipping_order_id;

END LOOP;
END LOOP;

    RAISE NOTICE 'Geração de histórico de pedidos concluída com sucesso!';
END $$;

-- SQL Mock Data Generation Script - Part 3
-- Target: PostgreSQL
-- Project: Purchase Order Generation for Stock Replenishment
-- Description: Creates purchase_orders and purchase_order_items for products with low or zero stock.

DO $$
DECLARE
    -- ### VARIÁVEIS DE CONTROLE ###
v_product_to_restock RECORD;
    v_purchaser_id UUID;
    v_purchase_order_id UUID;
    v_created_at_timestamp TIMESTAMP;
    v_purchase_quantity INT;
    v_purchase_price NUMERIC(19,4);
    v_purchase_total_price NUMERIC(38,2);

BEGIN
    RAISE NOTICE 'Iniciando a geração de Ordens de Compra para reposição de estoque...';

    -- 1. Seleciona um funcionário responsável pelas compras (não um vendedor).
SELECT id INTO v_purchaser_id
FROM public.employee
WHERE role IN ('LOCAL_MANAGER', 'STORAGE') AND active = true
    LIMIT 1;

IF v_purchaser_id IS NULL THEN
        RAISE EXCEPTION 'Nenhum funcionário com cargo de LOCAL_MANAGER ou STORAGE encontrado para ser o comprador.';
END IF;

    -- 2. Loop principal: Itera sobre cada produto que tem estoque zerado ou muito baixo (menor que 10).
    --    Isso inclui os produtos que definimos como zerados inicialmente e aqueles que podem ter se esgotado com as vendas.
FOR v_product_to_restock IN
SELECT p.sku, s.product_quantity
FROM public.product p
         JOIN public.storage s ON p.sku = s.product_sku
WHERE p.active = true AND s.product_quantity <= 10
    LOOP
        RAISE NOTICE 'Gerando Ordem de Compra para o produto: %', v_product_to_restock.sku;

-- ### ETAPA A: Definir os detalhes da compra ###
v_created_at_timestamp := NOW() - (floor(random() * 10) * '1 day'::interval); -- Ordem de compra criada nos últimos 10 dias
        v_purchase_quantity := floor(random() * (300 - 150 + 1) + 150)::INT; -- Compra entre 150 e 300 unidades
        v_purchase_price := round((random() * (100 - 5) + 5)::numeric, 4); -- Custo de compra simulado
        v_purchase_total_price := v_purchase_price * v_purchase_quantity;

        -- ### ETAPA B: Criar o `purchase_order` ###
INSERT INTO public.purchase_order (id, created_at, purchaseorderstatus, purchase_total_price_amount, purchase_total_product_amount, purchaser_id, shipping_order_id)
VALUES (
           gen_random_uuid(),
           v_created_at_timestamp,
           'INVOICED', -- Assume que a compra já foi faturada pelo fornecedor
           v_purchase_total_price,
           v_purchase_quantity,
           v_purchaser_id,
           NULL -- Opcional: Poderia estar vinculado a um shipping_order de entrada
       )
    RETURNING id INTO v_purchase_order_id;

-- ### ETAPA C: Criar o `purchase_order_item` correspondente ###
INSERT INTO public.purchase_order_item (id, created_at, purchase_price, purchase_quantity, purchase_product_sku, purchase_order_id)
VALUES (
           gen_random_uuid(),
           v_created_at_timestamp,
           v_purchase_price,
           v_purchase_quantity,
           v_product_to_restock.sku,
           v_purchase_order_id
       );

-- Opcional: Logicamente, o próximo passo em um sistema real seria um processo para receber
-- esta mercadoria e atualizar a tabela `storage`. Para este script, apenas geramos o histórico de compra.

END LOOP;

    RAISE NOTICE 'Geração de Ordens de Compra concluída com sucesso!';
END $$;