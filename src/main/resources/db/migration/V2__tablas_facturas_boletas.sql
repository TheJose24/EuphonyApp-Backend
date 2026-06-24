-- Create FACTURA table
CREATE TABLE FACTURA (
                         id_factura SERIAL PRIMARY KEY,
                         id_usuario UUID UNIQUE NOT NULL,
                         id_suscripcion INT,
                         id_metodo_pago INT NOT NULL,
                         numero_factura VARCHAR(20) UNIQUE NOT NULL,
                         fecha_emision TIMESTAMP NOT NULL,
                         monto_total DECIMAL(10, 2) NOT NULL,
                         impuesto DECIMAL(10, 2) NOT NULL,
                         estado VARCHAR(20) DEFAULT 'pendiente' CHECK (estado IN ('pagada', 'pendiente', 'anulada')),
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         CONSTRAINT fk_factura_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO(id_usuario) ON DELETE RESTRICT,
                         CONSTRAINT fk_factura_suscripcion FOREIGN KEY (id_suscripcion) REFERENCES SUSCRIPCION(id_suscripcion) ON DELETE SET NULL,
                         CONSTRAINT fk_factura_metodo_pago FOREIGN KEY (id_metodo_pago) REFERENCES METODO_PAGO(id_metodo_pago) ON DELETE RESTRICT
);

-- Create DETALLE_FACTURA table
CREATE TABLE DETALLE_FACTURA (
                                 id_detalle_factura SERIAL PRIMARY KEY,
                                 id_factura INT NOT NULL,
                                 descripcion VARCHAR(255) NOT NULL,
                                 cantidad INT NOT NULL,
                                 precio_unitario DECIMAL(10, 2) NOT NULL,
                                 subtotal DECIMAL(10, 2) GENERATED ALWAYS AS (cantidad * precio_unitario) STORED,
                                 CONSTRAINT fk_detalle_factura_factura FOREIGN KEY (id_factura) REFERENCES FACTURA(id_factura) ON DELETE CASCADE
);

-- Create BOLETA table
CREATE TABLE BOLETA (
                        id_boleta SERIAL PRIMARY KEY,
                        id_usuario UUID UNIQUE NOT NULL,
                        id_suscripcion INT,
                        id_metodo_pago INT NOT NULL,
                        numero_boleta VARCHAR(20) UNIQUE NOT NULL,
                        fecha_emision TIMESTAMP NOT NULL,
                        monto_total DECIMAL(10, 2) NOT NULL,
                        estado VARCHAR(20) DEFAULT 'pendiente' CHECK (estado IN ('pagada', 'pendiente', 'anulada')),
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT fk_boleta_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO(id_usuario) ON DELETE RESTRICT,
                        CONSTRAINT fk_boleta_suscripcion FOREIGN KEY (id_suscripcion) REFERENCES SUSCRIPCION(id_suscripcion) ON DELETE SET NULL,
                        CONSTRAINT fk_boleta_metodo_pago FOREIGN KEY (id_metodo_pago) REFERENCES METODO_PAGO(id_metodo_pago) ON DELETE RESTRICT
);

-- Create DETALLE_BOLETA table
CREATE TABLE DETALLE_BOLETA (
                                id_detalle_boleta SERIAL PRIMARY KEY,
                                id_boleta INT NOT NULL,
                                descripcion VARCHAR(255) NOT NULL,
                                cantidad INT NOT NULL,
                                precio_unitario DECIMAL(10, 2) NOT NULL,
                                subtotal DECIMAL(10, 2) GENERATED ALWAYS AS (cantidad * precio_unitario) STORED,
                                CONSTRAINT fk_detalle_boleta_boleta FOREIGN KEY (id_boleta) REFERENCES BOLETA(id_boleta) ON DELETE CASCADE
);
