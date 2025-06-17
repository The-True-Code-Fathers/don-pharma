-- ##################################################################
-- ##        SCRIPT DE DADOS DEFINITIVO - VERSÃO CORRIGIDA         ##
-- ##################################################################
-- Descrição: Versão final que corrige erros de sintaxe (RAISE NOTICE)
-- e de lógica (NOT NULL constraint em shipping_order).

-- ETAPA 1: LIMPEZA COMPLETA DO BANCO DE DADOS
DELETE FROM public.purchase_order_item;
DELETE FROM public.purchase_order;
DELETE FROM public.order_item;
DELETE FROM public.payment;
DELETE FROM public.orders;
DELETE FROM public.shipping_order;
DELETE FROM public.shipping_area;
DELETE FROM public.storage;
DELETE FROM public.shipping_provider;
DELETE FROM public.product;
DELETE FROM public.employee;

-- ETAPA 2: ESTRUTURA DA EMPRESA
-- Inserindo um quadro de funcionários maior e mais diverso
INSERT INTO public.employee (id, active, birthdate, created_at, fullname, gender, role) VALUES
-- Gerência e Admin
(gen_random_uuid(), true, '1988-05-12', NOW() - interval '5 year', 'Carlos Santana', 'MALE', 'LOCAL_MANAGER'),
(gen_random_uuid(), true, '1992-08-20', NOW() - interval '4 year', 'Fernanda Lima', 'FEMALE', 'FINANCIAL'),
(gen_random_uuid(), true, '1995-01-30', NOW() - interval '3 year', 'Ricardo Souza', 'MALE', 'HR'),
(gen_random_uuid(), true, '1991-06-10', NOW() - interval '4 year', 'André Costa', 'MALE', 'FINANCIAL'),
(gen_random_uuid(), true, '1993-02-01', NOW() - interval '3 year', 'Mariana Azevedo', 'FEMALE', 'HR'),
-- Vendedores (equipe expandida)
(gen_random_uuid(), true, '1990-09-05', NOW() - interval '4 year', 'Roberto Pereira', 'MALE', 'SALES'),
(gen_random_uuid(), true, '1996-02-18', NOW() - interval '3 year', 'Aline Gomes', 'FEMALE', 'SALES'),
(gen_random_uuid(), true, '1994-06-22', NOW() - interval '3 year', 'Marcos Rodrigues', 'MALE', 'SALES'),
(gen_random_uuid(), true, '1999-04-12', NOW() - interval '2 year', 'Gabriela Ferreira', 'FEMALE', 'SALES'),
(gen_random_uuid(), true, '1998-11-11', NOW() - interval '2 year', 'Bruno Alves', 'MALE', 'SALES'),
(gen_random_uuid(), true, '2000-07-19', NOW() - interval '1 year', 'Letícia Barros', 'FEMALE', 'SALES'),
(gen_random_uuid(), true, '1997-10-08', NOW() - interval '2 year', 'João Silva', 'MALE', 'SALES'),
(gen_random_uuid(), false, '1995-05-25', NOW() - interval '3 year', 'Vinicius Moraes', 'MALE', 'SALES'),
-- Operacional
(gen_random_uuid(), true, '1998-11-10', NOW() - interval '2 year', 'Beatriz Martins', 'FEMALE', 'SAC'),
(gen_random_uuid(), true, '2001-03-15', NOW() - interval '1 year', 'Camila Dias', 'FEMALE', 'SAC'),
(gen_random_uuid(), true, '2000-03-25', NOW() - interval '2 year', 'Lucas Farias', 'MALE', 'STORAGE'),
(gen_random_uuid(), true, '1993-07-15', NOW() - interval '3 year', 'Juliana Nunes', 'FEMALE', 'SHIPPING'),
(gen_random_uuid(), true, '1999-09-02', NOW() - interval '1 year', 'Felipe Arruda', 'MALE', 'STORAGE'),
(gen_random_uuid(), true, '1996-12-20', NOW() - interval '3 year', 'Diego Santos', 'MALE', 'SHIPPING'),
(gen_random_uuid(), false, '1989-03-14', NOW() - interval '5 year', 'Patrícia Andrade', 'FEMALE', 'STORAGE');

-- Inserindo Transportadoras
INSERT INTO public.shipping_provider (id, active, average_delivery_days, base_price, cnpj, created_at, daily_capacity, name) VALUES
                                                                                                                                 (gen_random_uuid(), true, 5, 15.50, '11.222.333/0001-44', NOW() - interval '3 year', 5000, 'RápidoLog'),
                                                                                                                                 (gen_random_uuid(), true, 3, 18.00, '22.333.444/0001-55', NOW() - interval '2 year', 8000, 'ExpressBrasil'),
                                                                                                                                 (gen_random_uuid(), true, 8, 12.75, '33.444.555/0001-66', NOW() - interval '5 year', 3000, 'LogiSul'),
                                                                                                                                 (gen_random_uuid(), true, 4, 14.00, '44.555.666/0001-77', NOW() - interval '1 year', 10000, 'TransNorte');

INSERT INTO public.shipping_area (id, active, created_at, description, states, shipping_provider_id) VALUES
    (gen_random_uuid(), true, NOW() - interval '2 year', 'Região Sudeste', 'SP,RJ,ES,MG', (SELECT id FROM public.shipping_provider WHERE name = 'RápidoLog' LIMIT 1)),
(gen_random_uuid(), true, NOW() - interval '1 year', 'Capitais do Sul e Sudeste', 'SP,RJ,PR,SC,RS,BH,VT', (SELECT id FROM public.shipping_provider WHERE name = 'ExpressBrasil' LIMIT 1)),
(gen_random_uuid(), true, NOW() - interval '4 year', 'Todo o Brasil (exceto Norte)', 'SUL,SUDESTE,CENTRO-OESTE,NORDESTE', (SELECT id FROM public.shipping_provider WHERE name = 'LogiSul' LIMIT 1)),
(gen_random_uuid(), true, NOW() - interval '6 months', 'Regiões Norte e Nordeste', 'NORTE,NORDESTE', (SELECT id FROM public.shipping_provider WHERE name = 'TransNorte' LIMIT 1));

-- ETAPA 3: PRODUTOS E ESTOQUE INICIAL
INSERT INTO public.product (sku, active, created_at, description, measurementunit, name) VALUES
                                                                                             ('PROD001', true, NOW() - interval '2 year', 'Dipirona Sódica 500mg/mL, 20mL', 3, 'Dipirona Gotas'),
                                                                                             ('PROD002', true, NOW() - interval '2 year', 'Paracetamol 750mg, caixa com 20 comprimidos', 6, 'Paracetamol 750mg'),
                                                                                             ('PROD003', true, NOW() - interval '1 year', 'Ibuprofeno 400mg, caixa com 10 cápsulas', 6, 'Ibuprofeno 400mg'),
                                                                                             ('PROD005', true, NOW() - interval '6 months', 'Dorflex, caixa com 36 comprimidos', 6, 'Dorflex'),
                                                                                             ('PROD010', true, NOW() - interval '1 year', 'Vitamina C 1g, tubo com 10 comprimidos efervescentes', 6, 'Vitamina C Efervescente'),
                                                                                             ('PROD012', true, NOW() - interval '2 year', 'Suplemento de Vitamina D 2000UI, 30 cápsulas', 6, 'Vitamina D 2000UI'),
                                                                                             ('PROD013', true, NOW() - interval '6 months', 'Lavitan A-Z, caixa com 60 comprimidos', 6, 'Lavitan A-Z'),
                                                                                             ('PROD020', true, NOW() - interval '1 year', 'Protetor Solar FPS 50, 120mL', 3, 'Protetor Solar Sundown FPS 50'),
                                                                                             ('PROD021', true, NOW() - interval '2 year', 'Creme Hidratante Nivea, lata 56g', 1, 'Creme Nivea'),
                                                                                             ('PROD022', true, NOW() - interval '8 months', 'Sabonete Líquido Facial Actine, 140mL', 3, 'Sabonete Actine'),
                                                                                             ('PROD032', true, NOW() - interval '1 year', 'Pasta de Dente Colgate Total 12, 90g', 1, 'Colgate Total 12'),
                                                                                             ('PROD033', true, NOW() - interval '3 year', 'Fio Dental Oral-B, 50m', 7, 'Fio Dental Oral-B'),
                                                                                             ('PROD034', true, NOW() - interval '1 year', 'Desodorante Aerosol Rexona, 150mL', 3, 'Desodorante Rexona'),
                                                                                             ('PROD040', true, NOW() - interval '4 year', 'Band-Aid, caixa com 40 unidades', 6, 'Band-Aid'),
                                                                                             ('PROD041', true, NOW() - interval '3 year', 'Mertiolate Antisséptico, 30mL', 3, 'Mertiolate'),
                                                                                             ('PROD048', true, NOW() - interval '1 year', 'Soro Fisiológico, 500mL', 3, 'Soro Fisiológico'),
                                                                                             ('PROD050', true, NOW() - interval '2 year', 'Termômetro Digital G-Tech', 7, 'Termômetro Digital'),
                                                                                             ('PROD051', true, NOW() - interval '1 year', 'Xarope para Tosse Vick, 120mL', 3, 'Xarope Vick'),
                                                                                             ('PROD053', true, NOW() - interval '2 year', 'Antiácido Eno, sachê 5g', 1, 'Sal de Fruta Eno'),
                                                                                             ('PROD054', true, NOW() - interval '1 year', 'Antialérgico Loratadina 10mg, 12 comprimidos', 6, 'Loratadina'),
                                                                                             ('PROD056', true, NOW() - interval '1 year', 'Creme para Assaduras Hipoglós, 45g', 1, 'Hipoglós'),
                                                                                             ('PROD057', true, NOW() - interval '2 year', 'Fralda Descartável Pampers, pacote M', 6, 'Fralda Pampers M'),
                                                                                             ('PROD070', true, NOW() - interval '3 year', 'Teste de Gravidez Confirme, unidade', 7, 'Teste de Gravidez Confirme'),
                                                                                             ('PROD071', true, NOW() - interval '1 year', 'Preservativo Jontex, pacote com 3', 6, 'Preservativo Jontex'),
                                                                                             ('PROD086', true, NOW() - interval '1 year', 'Descongestionante Nasal Neosoro, 30mL', 3, 'Neosoro Adulto'),
                                                                                             ('PROD093', true, NOW() - interval '3 year', 'Pomada para Assaduras Bepantol, 30g', 1, 'Bepantol Baby'),
                                                                                             ('PROD110', true, NOW() - interval '1 year', 'Dramin B6, 10 comprimidos', 6, 'Dramin'),
                                                                                             ('PROD112', true, NOW() - interval '1 year', 'Luftal Gotas, 15mL', 8, 'Luftal Gotas'),
                                                                                             ('PROD114', true, NOW() - interval '1 year', 'Cataflampro Emulgel, 60g', 1, 'Cataflampro'),
                                                                                             ('PROD014', false, NOW() - interval '3 year', 'Ômega 3 1000mg, pote com 120 cápsulas', 6, 'Ômega 3');

INSERT INTO public.storage (id, product_sku, product_quantity)
SELECT gen_random_uuid(), sku,
       CASE
           WHEN not active THEN 0
           WHEN sku IN ('PROD001', 'PROD022') THEN 0
           ELSE floor(random() * (250 - 50 + 1) + 50)::int
END
FROM public.product;

-- ETAPA 4: HISTÓRICO FINANCEIRO (PAGAMENTOS MENSAIS)
DO $$
DECLARE
v_employee RECORD;
    v_payment_date DATE;
    v_gross_income NUMERIC(19,4);
    v_taxes NUMERIC(19,4);
BEGIN
FOR v_employee IN SELECT id, role FROM public.employee WHERE active = true LOOP
-- Define o salário base pelo cargo
SELECT CASE v_employee.role
           WHEN 'LOCAL_MANAGER' THEN 6000.00
           WHEN 'FINANCIAL' THEN 4000.00
           WHEN 'SALES' THEN 4500.00
           WHEN 'HR' THEN 3500.00
           WHEN 'SHIPPING' THEN 2500.00
           WHEN 'STORAGE' THEN 2200.00
           WHEN 'SAC' THEN 2000.00
           ELSE 1800.00
           END INTO v_gross_income;

-- Gera os últimos 4 pagamentos mensais para cada funcionário
FOR i IN 0..3 LOOP
            v_payment_date := (date_trunc('month', NOW()) - (i * interval '1 month') + interval '4 days')::date;
            -- Simula o imposto de renda
            v_taxes := CASE
                WHEN v_gross_income > 5000 THEN v_gross_income * 0.20
                WHEN v_gross_income > 3000 THEN v_gross_income * 0.12
                ELSE v_gross_income * 0.07
END;

INSERT INTO public.payment(id, active, amount_in_taxes, created_at, dental_insurance_amount, food_voucher_amount, gross_income, health_insurance_amount, meal_voucher_amount, profit_sharing_amount, employee_id)
VALUES (gen_random_uuid(), true, v_taxes, v_payment_date, 50.00, 300.00, v_gross_income, 400.00, 600.00, v_gross_income * 0.2, v_employee.id);
END LOOP;
END LOOP;
END $$;

-- ETAPA 5: SIMULAÇÃO DE VENDAS, ESTOQUE E COMPRAS
DO $$
DECLARE
    -- ### Configuração da Simulação ###
v_start_date DATE := '2025-04-01';
    v_end_date DATE := '2025-06-17';
    v_today DATE := '2025-06-17';
    v_stock_threshold INT := 40;

    -- ### Variáveis de Apoio ###
    v_seller_ids UUID[];
    v_purchaser_id UUID;
    v_current_date DATE;
    v_provider_record RECORD;

    -- ### Variáveis de Loop ###
    v_product_record RECORD;
    v_order_id UUID;
    v_shipping_order_id UUID;
    v_purchase_order_id UUID;
    v_created_at_timestamp TIMESTAMP;
    v_shipment_date DATE;
    v_delivery_date DATE;
    v_shipping_status VARCHAR(255);
    v_order_products_price NUMERIC(19,4);
    v_order_weight NUMERIC(38,2);
    v_item_quantity INT;
    v_item_price NUMERIC(19,4);
    v_purchase_quantity INT;
    v_purchase_price NUMERIC(19,4);

BEGIN
    -- Carrega dados de apoio
    v_seller_ids := ARRAY(SELECT id FROM public.employee WHERE role = 'SALES' AND active = true);
SELECT id INTO v_purchaser_id FROM public.employee WHERE role = 'LOCAL_MANAGER' LIMIT 1;

-- Tabela temporária para simular o estoque em tempo real
CREATE TEMP TABLE temp_stock ON COMMIT DROP AS SELECT product_sku, product_quantity FROM public.storage;

    -- Loop principal por cada dia de simulação
FOR v_current_date IN SELECT generate_series(v_start_date, v_end_date, '1 day'::interval) LOOP
                             -- Gera entre 2 e 8 pedidos por dia
                          FOR i IN 1..floor(random() * 7 + 2)::INT LOOP

                          v_created_at_timestamp := v_current_date + (floor(random()*60000 + 28800))::integer * '1 second'::interval;
v_order_products_price := 0;
            v_order_weight := 0;

            -- ## CORREÇÃO ##: Lógica de seleção da transportadora mais robusta
SELECT id, average_delivery_days INTO v_provider_record FROM public.shipping_provider WHERE active = true ORDER BY random() LIMIT 1;

v_shipment_date := v_current_date + (floor(random()*2+1) * interval '1 day');
            v_delivery_date := v_shipment_date + (v_provider_record.average_delivery_days * interval '1 day');

            IF random() < 0.03 AND v_delivery_date < v_today THEN
                v_shipping_status := 'ATRASADO';
            ELSIF v_delivery_date < v_today THEN
                v_shipping_status := 'ENTREGUE';
            ELSIF v_shipment_date <= v_today AND v_delivery_date >= v_today THEN
                v_shipping_status := 'EM_TRANSPORTE';
ELSE
                v_shipping_status := 'PENDENTE';
END IF;

INSERT INTO public.shipping_order (id, active, created_at, delivery_date, destinationcity, destinationstate, estimated_delivery_days, shipment_date, shipping_cost, status, weight, shippingprovider_id)
VALUES (gen_random_uuid(), true, v_created_at_timestamp, v_delivery_date, 'Cidade Exemplo', 'SP', v_provider_record.average_delivery_days, v_shipment_date, round((random() * 30 + 12)::numeric, 2), v_shipping_status, 0, v_provider_record.id)
    RETURNING id INTO v_shipping_order_id;

INSERT INTO public.orders (id, created_at, description, orderstatus, products_price, total_amount, seller_id, shipping_order_id)
VALUES (gen_random_uuid(), v_created_at_timestamp, 'Pedido de venda simulado', 'INVOICED', 0, 0, v_seller_ids[floor(random() * array_length(v_seller_ids, 1) + 1)], v_shipping_order_id)
    RETURNING id INTO v_order_id;

FOR j IN 1..floor(random() * 4 + 1)::INT LOOP
SELECT sku, temp_stock.product_quantity INTO v_product_record FROM public.product p
                                                                       JOIN temp_stock ON p.sku = temp_stock.product_sku
WHERE p.active = true AND temp_stock.product_quantity > 0 ORDER BY random() LIMIT 1;

IF FOUND THEN
                    v_item_quantity := floor(random() * 3 + 1)::INT;
                    v_item_price := round((random() * 150 + 8)::numeric, 4);
                    v_item_quantity := LEAST(v_item_quantity, v_product_record.product_quantity);

INSERT INTO public.order_item (id, created_at, price, quantity, orders_id, product_sku)
VALUES (gen_random_uuid(), v_created_at_timestamp, v_item_price, v_item_quantity, v_order_id, v_product_record.sku);

v_order_products_price := v_order_products_price + (v_item_price * v_item_quantity);
                    v_order_weight := v_order_weight + (0.5 * v_item_quantity);
UPDATE temp_stock SET product_quantity = product_quantity - v_item_quantity WHERE product_sku = v_product_record.sku;

IF (SELECT product_quantity FROM temp_stock WHERE product_sku = v_product_record.sku) < v_stock_threshold THEN
                        v_purchase_quantity := floor(random()*100+100)::int;
v_purchase_price := round((v_item_price * 0.6)::numeric, 4);

INSERT INTO public.purchase_order (id, created_at, purchaseorderstatus, purchase_total_price_amount, purchase_total_product_amount, purchaser_id)
VALUES (gen_random_uuid(), v_created_at_timestamp, 'OPEN', (v_purchase_price * v_purchase_quantity), v_purchase_quantity, v_purchaser_id)
    RETURNING id INTO v_purchase_order_id;

INSERT INTO public.purchase_order_item (id, created_at, purchase_price, purchase_quantity, purchase_product_sku, purchase_order_id)
VALUES (gen_random_uuid(), v_created_at_timestamp, v_purchase_price, v_purchase_quantity, v_product_record.sku, v_purchase_order_id);

UPDATE temp_stock SET product_quantity = product_quantity + v_purchase_quantity WHERE product_sku = v_product_record.sku;
END IF;
END IF;
END LOOP;

UPDATE public.orders o SET products_price = v_order_products_price, total_amount = v_order_products_price + s.shipping_cost FROM public.shipping_order s WHERE o.id = v_order_id AND s.id = o.shipping_order_id;
UPDATE public.shipping_order SET weight = v_order_weight WHERE id = v_shipping_order_id;
END LOOP;
END LOOP;

UPDATE public.storage s SET product_quantity = ts.product_quantity FROM temp_stock ts WHERE s.product_sku = ts.product_sku;
END $$;