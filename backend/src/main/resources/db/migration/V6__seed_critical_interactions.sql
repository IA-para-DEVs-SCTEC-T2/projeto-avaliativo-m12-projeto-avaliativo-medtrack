-- Seed de medicamentos comuns para interações
INSERT INTO medications (name, active_ingredient, description) VALUES
    ('Varfarina', 'Warfarin', 'Anticoagulante oral'),
    ('Aspirina', 'Ácido Acetilsalicílico', 'Anti-inflamatório e antiagregante plaquetário'),
    ('Ibuprofeno', 'Ibuprofen', 'Anti-inflamatório não esteroidal (AINE)'),
    ('Metformina', 'Metformin', 'Antidiabético oral'),
    ('Omeprazol', 'Omeprazole', 'Inibidor de bomba de prótons'),
    ('Losartana', 'Losartan', 'Anti-hipertensivo (BRA)'),
    ('Sinvastatina', 'Simvastatin', 'Estatina para colesterol'),
    ('Amoxicilina', 'Amoxicillin', 'Antibiótico penicilínico'),
    ('Fluoxetina', 'Fluoxetine', 'Antidepressivo ISRS'),
    ('Diazepam', 'Diazepam', 'Benzodiazepínico ansiolítico'),
    ('Atenolol', 'Atenolol', 'Beta-bloqueador'),
    ('Clopidogrel', 'Clopidogrel', 'Antiagregante plaquetário'),
    ('Lítio', 'Lithium', 'Estabilizador de humor'),
    ('Digoxina', 'Digoxin', 'Cardiotônico'),
    ('Ciprofloxacino', 'Ciprofloxacin', 'Antibiótico fluoroquinolona');

-- Seed de 15 interações críticas conhecidas
INSERT INTO interactions (medication_a_id, medication_b_id, severity, description, source) VALUES
    -- Varfarina + Aspirina = SEVERE (risco de sangramento)
    (1, 2, 'SEVERE', 'Risco aumentado de sangramento. Combinação pode causar hemorragia grave.', 'LOCAL'),
    -- Varfarina + Ibuprofeno = SEVERE (risco de sangramento)
    (1, 3, 'SEVERE', 'AINEs aumentam risco de sangramento com anticoagulantes.', 'LOCAL'),
    -- Varfarina + Omeprazol = MODERATE (altera metabolismo)
    (1, 5, 'MODERATE', 'Omeprazol pode aumentar efeito da varfarina por inibição do CYP2C19.', 'LOCAL'),
    -- Varfarina + Amoxicilina = MODERATE (altera flora intestinal)
    (1, 8, 'MODERATE', 'Antibióticos podem aumentar efeito anticoagulante por alteração da flora intestinal.', 'LOCAL'),
    -- Varfarina + Fluoxetina = SEVERE (inibição CYP2C9)
    (1, 9, 'SEVERE', 'Fluoxetina inibe CYP2C9, aumentando significativamente o efeito da varfarina.', 'LOCAL'),
    -- Metformina + Ciprofloxacino = MODERATE (hipoglicemia)
    (4, 15, 'MODERATE', 'Fluoroquinolonas podem causar hipo ou hiperglicemia com antidiabéticos.', 'LOCAL'),
    -- Sinvastatina + Amoxicilina = MILD (interação leve)
    (7, 8, 'MILD', 'Interação clinicamente pouco significativa. Monitorar sintomas musculares.', 'LOCAL'),
    -- Fluoxetina + Diazepam = MODERATE (sedação aumentada)
    (9, 10, 'MODERATE', 'Fluoxetina pode aumentar níveis de diazepam por inibição do CYP2C19.', 'LOCAL'),
    -- Fluoxetina + Lítio = SEVERE (síndrome serotoninérgica)
    (9, 13, 'SEVERE', 'Risco de síndrome serotoninérgica. Monitoramento rigoroso necessário.', 'LOCAL'),
    -- Losartana + Lítio = SEVERE (toxicidade do lítio)
    (6, 13, 'SEVERE', 'BRAs podem aumentar níveis de lítio, causando toxicidade.', 'LOCAL'),
    -- Ibuprofeno + Losartana = MODERATE (reduz efeito anti-hipertensivo)
    (3, 6, 'MODERATE', 'AINEs podem reduzir efeito anti-hipertensivo e aumentar risco renal.', 'LOCAL'),
    -- Aspirina + Clopidogrel = MODERATE (sangramento aumentado, mas combinação comum)
    (2, 12, 'MODERATE', 'Risco aumentado de sangramento, mas combinação terapêutica comum (dupla antiagregação).', 'LOCAL'),
    -- Digoxina + Omeprazol = MILD (absorção alterada)
    (14, 5, 'MILD', 'Omeprazol pode aumentar levemente absorção de digoxina.', 'LOCAL'),
    -- Atenolol + Diazepam = MILD (hipotensão)
    (11, 10, 'MILD', 'Possível potencialização do efeito hipotensor. Monitorar pressão arterial.', 'LOCAL'),
    -- Clopidogrel + Omeprazol = SEVERE (reduz eficácia do clopidogrel)
    (12, 5, 'SEVERE', 'Omeprazol inibe CYP2C19, reduzindo conversão do clopidogrel em metabólito ativo.', 'LOCAL');
