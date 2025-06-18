-- ##################################################################
-- ##      SCRIPT DE DADOS DEFINITIVO - VERSÃO SEM total_amount    ##
-- ##################################################################
-- Descrição: Versão final ajustada para ser compatível com a tabela
-- 'orders' após a remoção da coluna 'total_amount'.
-- MODIFICAÇÃO: As quantidades em 'order_item' e o estoque inicial
-- foram multiplicados por 20 para gerar uma receita maior.

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
INSERT INTO public.employee (id, active, birthdate, created_at, fullname, gender, role) VALUES
                                                                                            (gen_random_uuid(), true, '1988-05-12', NOW() - interval '5 year', 'Carlos Santana', 'MALE', 'LOCAL_MANAGER'),
                                                                                            (gen_random_uuid(), true, '1992-08-20', NOW() - interval '4 year', 'Fernanda Lima', 'FEMALE', 'FINANCIAL'),
                                                                                            (gen_random_uuid(), true, '1995-01-30', NOW() - interval '3 year', 'Ricardo Souza', 'MALE', 'HR'),
                                                                                            (gen_random_uuid(), true, '1990-09-05', NOW() - interval '4 year', 'Roberto Pereira', 'MALE', 'SALES'),
                                                                                            (gen_random_uuid(), true, '1996-02-18', NOW() - interval '3 year', 'Aline Gomes', 'FEMALE', 'SALES'),
                                                                                            (gen_random_uuid(), true, '1994-06-22', NOW() - interval '3 year', 'Marcos Rodrigues', 'MALE', 'SALES'),
                                                                                            (gen_random_uuid(), true, '1999-04-12', NOW() - interval '2 year', 'Gabriela Ferreira', 'FEMALE', 'SALES'),
                                                                                            (gen_random_uuid(), true, '1998-11-10', NOW() - interval '2 year', 'Beatriz Martins', 'FEMALE', 'SAC'),
                                                                                            (gen_random_uuid(), true, '2000-03-25', NOW() - interval '2 year', 'Lucas Farias', 'MALE', 'STORAGE'),
                                                                                            (gen_random_uuid(), true, '1993-07-15', NOW() - interval '3 year', 'Juliana Nunes', 'FEMALE', 'SHIPPING'),
                                                                                            (gen_random_uuid(), false, '1989-03-14', NOW() - interval '5 year', 'Patrícia Andrade', 'FEMALE', 'STORAGE');


INSERT INTO public.shipping_provider (id, active, average_delivery_days, base_price, cnpj, created_at, daily_capacity, name) VALUES
                                                                                                                                 (gen_random_uuid(), true, 5, 15.50, '11.222.333/0001-44', NOW() - interval '3 year', 5000, 'RápidoLog'),
                                                                                                                                 (gen_random_uuid(), true, 3, 18.00, '22.333.444/0001-55', NOW() - interval '2 year', 8000, 'ExpressBrasil');

INSERT INTO public.shipping_area (id, active, created_at, description, states, shipping_provider_id) VALUES
    (gen_random_uuid(), true, NOW() - interval '2 year', 'Região Sudeste', 'SP,RJ,ES,MG', (SELECT id FROM public.shipping_provider WHERE name = 'RápidoLog' LIMIT 1)),
(gen_random_uuid(), true, NOW() - interval '1 year', 'Capitais do Sul e Sudeste', 'SP,RJ,PR,SC,RS,BH,VT', (SELECT id FROM public.shipping_provider WHERE name = 'ExpressBrasil' LIMIT 1));

-- ETAPA 3: PRODUTOS E ESTOQUE INICIAL
INSERT INTO public.product (sku, active, created_at, description, measurementunit, name) VALUES
                                                                                             ('PROD001', true, NOW() - interval '2 year', 'Dipirona Sódica 500mg/mL, 20mL', 3, 'Dipirona Gotas'),
                                                                                             ('PROD002', true, NOW() - interval '2 year', 'Paracetamol 750mg, caixa com 20 comprimidos', 6, 'Paracetamol 750mg'),
                                                                                             ('PROD003', true, NOW() - interval '1 year', 'Ibuprofeno 400mg, caixa com 10 cápsulas', 6, 'Ibuprofeno 400mg'),
                                                                                             ('PROD005', true, NOW() - interval '6 months', 'Dorflex, caixa com 36 comprimidos', 6, 'Dorflex'),
                                                                                             ('PROD010', true, NOW() - interval '1 year', 'Vitamina C 1g, tubo com 10 comprimidos efervescentes', 6, 'Vitamina C Efervescente'),
                                                                                             ('PROD020', true, NOW() - interval '1 year', 'Protetor Solar FPS 50, 120mL', 3, 'Protetor Solar Sundown FPS 50'),
                                                                                             ('PROD021', true, NOW() - interval '2 year', 'Creme Hidratante Nivea, lata 56g', 1, 'Creme Nivea'),
                                                                                             ('PROD032', true, NOW() - interval '1 year', 'Pasta de Dente Colgate Total 12, 90g', 1, 'Colgate Total 12'),
                                                                                             ('PROD040', true, NOW() - interval '4 year', 'Band-Aid, caixa com 40 unidades', 6, 'Band-Aid'),
                                                                                             ('PROD056', true, NOW() - interval '1 year', 'Creme para Assaduras Hipoglós, 45g', 1, 'Hipoglós'),
                                                                                             ('PROD057', true, NOW() - interval '2 year', 'Fralda Descartável Pampers, pacote M', 6, 'Fralda Pampers M'),
                                                                                             ('PROD071', true, NOW() - interval '1 year', 'Preservativo Jontex, pacote com 3', 6, 'Preservativo Jontex'),
                                                                                             ('PROD110', true, NOW() - interval '1 year', 'Dramin B6, 10 comprimidos', 6, 'Dramin'),
                                                                                             ('PROD112', true, NOW() - interval '1 year', 'Luftal Gotas, 15mL', 8, 'Luftal Gotas'),
                                                                                             ('PROD014', false, NOW() - interval '3 year', 'Ômega 3 1000mg, pote com 120 cápsulas', 6, 'Ômega 3');

INSERT INTO public.storage (id, product_sku, product_quantity)
SELECT gen_random_uuid(), sku,
       -- ## MODIFICAÇÃO ##: Quantidade inicial de estoque multiplicada por 20 para suportar o aumento das vendas.
       CASE WHEN not active THEN 0 ELSE (floor(random() * (200 - 50 + 1) + 50)::int) * 20 END
FROM public.product;

-- ETAPA 4: CRIAÇÃO DE ORDENS DE COMPRA (PURCHASE ORDERS) DE DEMONSTRAÇÃO
DO $$
DECLARE
v_purchaser_id UUID;
    v_purchase_order_id UUID;
BEGIN
SELECT id INTO v_purchaser_id FROM public.employee WHERE role = 'LOCAL_MANAGER' LIMIT 1;

INSERT INTO public.purchase_order (id, created_at, purchaseorderstatus, purchase_total_price_amount, purchase_total_product_amount, purchaser_id)
VALUES (gen_random_uuid(), '2025-03-10 10:00:00', 'INVOICED', (150 * 18.50), 150, v_purchaser_id)
    RETURNING id INTO v_purchase_order_id;
INSERT INTO public.purchase_order_item (id, created_at, purchase_price, purchase_quantity, purchase_product_sku, purchase_order_id)
VALUES (gen_random_uuid(), '2025-03-10 10:00:00', 18.50, 150, 'PROD005', v_purchase_order_id);

INSERT INTO public.purchase_order (id, created_at, purchaseorderstatus, purchase_total_price_amount, purchase_total_product_amount, purchaser_id)
VALUES (gen_random_uuid(), '2025-03-12 11:30:00', 'INVOICED', (200 * 8.20), 200, v_purchaser_id)
    RETURNING id INTO v_purchase_order_id;
INSERT INTO public.purchase_order_item (id, created_at, purchase_price, purchase_quantity, purchase_product_sku, purchase_order_id)
VALUES (gen_random_uuid(), '2025-03-12 11:30:00', 8.20, 200, 'PROD002', v_purchase_order_id);

INSERT INTO public.purchase_order (id, created_at, purchaseorderstatus, purchase_total_price_amount, purchase_total_product_amount, purchaser_id)
VALUES (gen_random_uuid(), '2025-03-15 09:00:00', 'INVOICED', (100 * 35.00), 100, v_purchaser_id)
    RETURNING id INTO v_purchase_order_id;
INSERT INTO public.purchase_order_item (id, created_at, purchase_price, purchase_quantity, purchase_product_sku, purchase_order_id)
VALUES (gen_random_uuid(), '2025-03-15 09:00:00', 35.00, 100, 'PROD057', v_purchase_order_id);
END $$;


-- ETAPA 5: HISTÓRICO FINANCEIRO REDUZIDO (Maio e Junho de 2025)
DO $$
DECLARE
v_employee RECORD;
    v_payment_date DATE;
    v_gross_income NUMERIC(19,4);
BEGIN
FOR v_employee IN SELECT id, role FROM public.employee WHERE active = true LOOP
SELECT CASE v_employee.role
           WHEN 'LOCAL_MANAGER' THEN 6000.00 WHEN 'FINANCIAL' THEN 4000.00
           WHEN 'SALES' THEN 4500.00 WHEN 'HR' THEN 3500.00
           ELSE 2500.00
           END INTO v_gross_income;

FOR i IN 0..1 LOOP
            v_payment_date := (date_trunc('month', '2025-06-17'::date) - (i * interval '1 month') + interval '4 days')::date;
INSERT INTO public.payment(id, active, amount_in_taxes, created_at, dental_insurance_amount, food_voucher_amount, gross_income, health_insurance_amount, meal_voucher_amount, profit_sharing_amount, employee_id)
VALUES (gen_random_uuid(), true, v_gross_income * 0.1, v_payment_date, 50.00, 300.00, v_gross_income, 400.00, 600.00, v_gross_income * 0.2, v_employee.id);
END LOOP;
END LOOP;
END $$;

-- ETAPA 6: SIMULAÇÃO DE VENDAS (SEM A COLUNA total_amount)
DO $$
DECLARE
v_start_date DATE := '2025-04-01';
    v_end_date DATE := '2025-06-17';
    v_today DATE := '2025-06-17';
    v_seller_ids UUID[];
    v_current_date DATE;
    v_provider_record RECORD;
    v_product_record RECORD;
    v_order_id UUID;
    v_shipping_order_id UUID;
    v_created_at_timestamp TIMESTAMP;
    v_shipment_date DATE;
    v_delivery_date DATE;
    v_shipping_status VARCHAR(255);
    v_order_status VARCHAR(255);
    v_order_products_price NUMERIC(19,4);
    v_order_weight NUMERIC(38,2);
    v_item_quantity INT;
    v_item_price NUMERIC(19,4);

BEGIN
    v_seller_ids := ARRAY(SELECT id FROM public.employee WHERE role = 'SALES' AND active = true);

    CREATE TEMP TABLE temp_stock ON COMMIT DROP AS SELECT product_sku, product_quantity FROM public.storage;

FOR v_current_date IN SELECT generate_series(v_start_date, v_end_date, '1 day'::interval) LOOP
                          IF EXTRACT(ISODOW FROM v_current_date) IN (1, 3, 5) THEN
            FOR i IN 1..floor(random() * 4 + 1)::INT LOOP
                v_created_at_timestamp := v_current_date + (floor(random()*60000 + 28800))::integer * '1 second'::interval;

IF v_current_date >= (v_today - interval '2 days') THEN v_order_status := 'OPEN';
                ELSIF random() < 0.08 THEN v_order_status := 'CANCELLED';
ELSE v_order_status := 'INVOICED';
END IF;

                -- ## AJUSTE ##: Coluna 'total_amount' removida do INSERT
INSERT INTO public.orders (id, created_at, description, orderstatus, products_price, seller_id, shipping_order_id)
VALUES (gen_random_uuid(), v_created_at_timestamp, 'Pedido com status variado', v_order_status, 0, v_seller_ids[floor(random() * array_length(v_seller_ids, 1) + 1)], NULL)
    RETURNING id INTO v_order_id;

IF v_order_status IN ('OPEN', 'INVOICED') THEN
                    v_order_products_price := 0;
                    v_order_weight := 0;

SELECT id, average_delivery_days INTO v_provider_record FROM public.shipping_provider WHERE active = true ORDER BY random() LIMIT 1;
v_shipment_date := v_current_date + (floor(random()*2+1) * interval '1 day');
                    v_delivery_date := v_shipment_date + (v_provider_record.average_delivery_days * interval '1 day');

                    IF random() < 0.03 AND v_delivery_date < v_today THEN v_shipping_status := 'ATRASADO';
                    ELSIF v_delivery_date < v_today THEN v_shipping_status := 'ENTREGUE';
                    ELSIF v_shipment_date <= v_today AND v_delivery_date >= v_today THEN v_shipping_status := 'EM_TRANSPORTE';
ELSE v_shipping_status := 'PENDENTE';
END IF;

INSERT INTO public.shipping_order (id, active, created_at, delivery_date, destinationcity, destinationstate, estimated_delivery_days, shipment_date, shipping_cost, status, weight, shippingprovider_id)
VALUES (gen_random_uuid(), true, v_created_at_timestamp, v_delivery_date, 'Cidade Exemplo', 'SP', v_provider_record.average_delivery_days, v_shipment_date, round((random() * 30 + 12)::numeric, 2), v_shipping_status, 0, v_provider_record.id)
    RETURNING id INTO v_shipping_order_id;

FOR j IN 1..floor(random() * 3 + 1)::INT LOOP
SELECT sku, temp_stock.product_quantity INTO v_product_record FROM public.product p
                                                                       JOIN temp_stock ON p.sku = temp_stock.product_sku
WHERE p.active = true AND temp_stock.product_quantity > 0 ORDER BY random() LIMIT 1;

IF FOUND THEN
                            -- ## MODIFICAÇÃO ##: Quantidade do item multiplicada por 20.
                            v_item_quantity := (floor(random() * 2 + 1)::INT) * 20;
                            v_item_price := round((random() * 150 + 8)::numeric, 4);
                            v_item_quantity := LEAST(v_item_quantity, v_product_record.product_quantity);

INSERT INTO public.order_item (id, created_at, price, quantity, orders_id, product_sku)
VALUES (gen_random_uuid(), v_created_at_timestamp, v_item_price, v_item_quantity, v_order_id, v_product_record.sku);

v_order_products_price := v_order_products_price + (v_item_price * v_item_quantity);
                            v_order_weight := v_order_weight + (0.5 * v_item_quantity);
UPDATE temp_stock SET product_quantity = product_quantity - v_item_quantity WHERE product_sku = v_product_record.sku;
END IF;
END LOOP;

                    -- ## AJUSTE ##: Coluna 'total_amount' removida do UPDATE
UPDATE public.orders o SET products_price = v_order_products_price, shipping_order_id = v_shipping_order_id WHERE o.id = v_order_id;
UPDATE public.shipping_order SET weight = v_order_weight WHERE id = v_shipping_order_id;
END IF;
END LOOP;
END IF;
END LOOP;

UPDATE public.storage s SET product_quantity = ts.product_quantity FROM temp_stock ts WHERE s.product_sku = ts.product_sku;
END $$;