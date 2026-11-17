import os
import sys
from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether, HRFlowable
)
from reportlab.pdfgen import canvas

class NumberedCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_header_footer(num_pages)
            super().showPage()
        super().save()

    def draw_header_footer(self, page_count):
        self.saveState()
        # Header (pages > 1)
        if self._pageNumber > 1:
            self.setFont("Helvetica-Bold", 8)
            self.setFillColor(colors.HexColor("#1A365D")) # Navy
            self.drawString(40, 755, "SISTEMA DE GESTIÓN DE CARTERA DE CRÉDITOS Y COBRANZAS")
            self.setFont("Helvetica", 8)
            self.setFillColor(colors.HexColor("#718096"))
            self.drawRightString(letter[0] - 40, 755, "DICCIONARIO DE DATOS (POSTGRESQL)")
            self.setStrokeColor(colors.HexColor("#CBD5E0"))
            self.setLineWidth(0.6)
            self.line(40, 747, letter[0] - 40, 747)

        # Footer (all pages)
        self.setFont("Helvetica", 8)
        self.setFillColor(colors.HexColor("#718096"))
        self.drawString(40, 30, "Universidad Distrital Francisco José de Caldas • Ingeniería de Sistemas • Bases de Datos")
        page_str = f"Página {self._pageNumber} de {page_count}"
        self.drawRightString(letter[0] - 40, 30, page_str)
        self.setStrokeColor(colors.HexColor("#CBD5E0"))
        self.setLineWidth(0.6)
        self.line(40, 42, letter[0] - 40, 42)
        self.restoreState()

def build_pdf(filename):
    doc = SimpleDocTemplate(
        filename,
        pagesize=letter,
        leftMargin=40,
        rightMargin=40,
        topMargin=48,
        bottomMargin=48
    )

    styles = getSampleStyleSheet()

    # Custom styles
    title_style = ParagraphStyle(
        'DocTitle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=20,
        leading=24,
        textColor=colors.HexColor('#1A365D'),
        alignment=0,
        spaceAfter=4
    )

    subtitle_style = ParagraphStyle(
        'DocSubtitle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=12,
        leading=16,
        textColor=colors.HexColor('#2B6CB0'),
        spaceAfter=12
    )

    meta_style = ParagraphStyle(
        'MetaText',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9,
        leading=13,
        textColor=colors.HexColor('#4A5568')
    )

    h1_style = ParagraphStyle(
        'SectionH1',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=13,
        leading=17,
        textColor=colors.HexColor('#1A365D'),
        spaceBefore=12,
        spaceAfter=6,
        keepWithNext=True
    )

    body_style = ParagraphStyle(
        'BodyDark',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9,
        leading=13,
        textColor=colors.HexColor('#2D3748')
    )

    table_header_style = ParagraphStyle(
        'TH',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=8,
        leading=10,
        textColor=colors.white,
        alignment=1
    )

    cell_style = ParagraphStyle(
        'TD',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=7.5,
        leading=9.5,
        textColor=colors.HexColor('#2D3748')
    )

    cell_bold = ParagraphStyle(
        'TDBold',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=7.5,
        leading=9.5,
        textColor=colors.HexColor('#1A365D')
    )

    cell_mono = ParagraphStyle(
        'TDMono',
        parent=styles['Normal'],
        fontName='Courier-Bold',
        fontSize=7.5,
        leading=9.5,
        textColor=colors.HexColor('#C53030')
    )

    story = []

    # Title Banner
    story.append(Paragraph("DICCIONARIO DE DATOS TÉCNICO", title_style))
    story.append(Paragraph("Sistema de Gestión de Cartera de Créditos y Cobranzas con Asignación Exclusiva por Cobrador", subtitle_style))
    story.append(HRFlowable(width="100%", thickness=2, color=colors.HexColor("#1A365D"), spaceAfter=10))

    # Metadata Box
    meta_data = [
        [
            Paragraph("<b>Investigador:</b> Jonathan Alexander Puentes Segura", meta_style),
            Paragraph("<b>Institución:</b> Universidad Distrital Francisco José de Caldas", meta_style)
        ],
        [
            Paragraph("<b>SGBD:</b> PostgreSQL 16+ Relacional", meta_style),
            Paragraph("<b>Base de Datos:</b> <code>cobranzas</code> (Schema: public)", meta_style)
        ],
        [
            Paragraph("<b>Arquitectura:</b> Normalizada 3FN con Vistas Materializadas", meta_style),
            Paragraph("<b>Estado del Esquema:</b> Producción / Implementado en Java + Spring Boot", meta_style)
        ]
    ]
    meta_table = Table(meta_data, colWidths=[270, 262])
    meta_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), colors.HexColor('#EDF2F7')),
        ('BOX', (0,0), (-1,-1), 1, colors.HexColor('#CBD5E0')),
        ('PADDING', (0,0), (-1,-1), 6),
        ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
    ]))
    story.append(meta_table)
    story.append(Spacer(1, 12))

    # Introduction
    story.append(Paragraph("<b>1. Resumen y Arquitectura del Diccionario</b>", h1_style))
    intro_p = Paragraph(
        "El presente documento técnico describe en detalle la estructura física y lógica de las seis tablas base, "
        "índices, llaves primarias/foráneas, disparadores (triggers) y vistas (estándar y materializada) que componen la base de datos "
        "<b>cobranzas</b>. El modelo responde a estrictas reglas de negocio financieras: asignación exclusiva y unívoca de cartera por cobrador, "
        "cálculo automatizado de tasas de interés según plazo/duración (7 días al 5%, 15 días al 10%, un mes o más al 20%), "
        "y control transaccional estricto para refinanciamientos encadenados condicionados al pago total del interés generado.",
        body_style
    )
    story.append(intro_p)
    story.append(Spacer(1, 10))

    # Helper function to generate Table Definitions
    def create_table_doc(title, desc, columns):
        elements = []
        elements.append(Paragraph(f"<b>{title}</b>", h1_style))
        elements.append(Paragraph(desc, body_style))
        elements.append(Spacer(1, 4))

        col_widths = [85, 80, 42, 60, 60, 205]
        header_row = [
            Paragraph("Columna", table_header_style),
            Paragraph("Tipo de Dato", table_header_style),
            Paragraph("Nulo", table_header_style),
            Paragraph("Clave", table_header_style),
            Paragraph("Valor Defecto", table_header_style),
            Paragraph("Descripción / Regla de Negocio", table_header_style)
        ]
        rows = [header_row]

        for col in columns:
            name_p = Paragraph(col[0], cell_bold)
            type_p = Paragraph(col[1], cell_style)
            null_p = Paragraph(col[2], cell_style)
            key_p = Paragraph(col[3], cell_mono if "PK" in col[3] or "FK" in col[3] else cell_style)
            def_p = Paragraph(col[4], cell_style)
            desc_p = Paragraph(col[5], cell_style)
            rows.append([name_p, type_p, null_p, key_p, def_p, desc_p])

        t = Table(rows, colWidths=col_widths, repeatRows=1)
        t.setStyle(TableStyle([
            ('BACKGROUND', (0,0), (-1,0), colors.HexColor('#1A365D')),
            ('ALIGN', (0,0), (-1,0), 'CENTER'),
            ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor('#CBD5E0')),
            ('VALIGN', (0,0), (-1,-1), 'TOP'),
            ('PADDING', (0,0), (-1,-1), 4),
            ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor('#F7FAFC')])
        ]))
        elements.append(t)
        elements.append(Spacer(1, 10))
        return elements

    # 1. CLIENTES
    clientes_cols = [
        ("id", "INTEGER", "NO", "PK", "GENERATED IDENTITY", "Identificador único secuencial del cliente o deudor."),
        ("nombre_cliente", "VARCHAR(150)", "NO", "-", "-", "Nombre completo y apellidos del deudor titular."),
        ("telefono_cliente", "VARCHAR(30)", "SÍ", "-", "NULL", "Número telefónico o móvil de contacto para cobro."),
        ("fecha_creacion", "TIMESTAMP", "NO", "-", "CURRENT_TIMESTAMP", "Fecha y hora exacta del registro en la plataforma.")
    ]
    story.extend(create_table_doc(
        "2. Tabla: clientes (Deudores)",
        "Almacena la información de las personas naturales o comerciales que adquieren obligaciones crediticias con la organización.",
        clientes_cols
    ))

    # 2. COBRADORES
    cobradores_cols = [
        ("id", "INTEGER", "NO", "PK", "GENERATED IDENTITY", "Identificador único asignado al agente de cobranza."),
        ("nombre_cobrador", "VARCHAR(150)", "NO", "-", "-", "Nombre completo del cobrador responsable."),
        ("telefono_cobrador", "VARCHAR(30)", "SÍ", "-", "NULL", "Teléfono de contacto corporativo del cobrador."),
        ("username", "VARCHAR(50)", "NO", "UNIQUE", "-", "Nombre de usuario exclusivo para autenticación en la App."),
        ("password", "VARCHAR(255)", "NO", "-", "-", "Contraseña para inicio de sesión seguro (hash/credencial)."),
        ("activo", "BOOLEAN", "NO", "-", "TRUE", "Indica si el cobrador puede recibir asignaciones y operar."),
        ("fecha_creacion", "TIMESTAMP", "NO", "-", "CURRENT_TIMESTAMP", "Fecha y hora de alta del cobrador en el sistema.")
    ]
    story.extend(create_table_doc(
        "3. Tabla: cobradores (Agentes de Campo)",
        "Contiene el padrón de cobradores y sus credenciales de acceso para garantizar que cada agente acceda únicamente a su cartera.",
        cobradores_cols
    ))

    # 3. CREDITOS
    creditos_cols = [
        ("id", "INTEGER", "NO", "PK", "GENERATED IDENTITY", "Identificador único del crédito u obligación."),
        ("credito_origen_id", "INTEGER", "SÍ", "FK / UNIQUE", "NULL", "Referencia al crédito previo. UNIQUE garantiza 1 refinanciamiento activo."),
        ("cliente_id", "INTEGER", "NO", "FK (clientes)", "-", "Identificador del cliente titular de la obligación."),
        ("cobrador_id", "INTEGER", "NO", "FK (cobradores)", "-", "Identificador del cobrador asignado de forma exclusiva."),
        ("monto", "DECIMAL(12,2)", "NO", "CHECK (> 0)", "-", "Capital principal otorgado en préstamo."),
        ("fecha_inicio", "DATE", "NO", "-", "-", "Fecha de desembolso y pacto inicial del crédito."),
        ("fecha_limite", "DATE", "NO", "CHECK (> inicio)", "-", "Fecha final de liquidación de la obligación."),
        ("frecuencia_pago", "VARCHAR(20)", "NO", "CHECK (DIARIA, SEMANAL, ...)", "-", "Periodicidad de cuotas: DIARIA, SEMANAL, QUINCENAL o MENSUAL."),
        ("tasa_interes", "DECIMAL(5,2)", "NO", "CHECK (5, 10, 20)", "0.00", "Porcentaje de interés calculado según duración (7d=5%, 15d=10%, >=28d=20%)."),
        ("estado", "VARCHAR(20)", "NO", "CHECK (AL_DIA, ...)", "'AL_DIA'", "Estado del crédito: AL_DIA, PAGADO o REFINANCIADO.")
    ]
    story.extend(create_table_doc(
        "4. Tabla: creditos (Obligaciones y Préstamos)",
        "Registra cada obligación financiera. Implementa la asignación exclusiva por cobrador y el refinanciamiento encadenado.",
        creditos_cols
    ))

    # 4. CUOTAS
    cuotas_cols = [
        ("id", "INTEGER", "NO", "PK", "GENERATED IDENTITY", "Identificador secuencial propio de la cuota."),
        ("credito_id", "INTEGER", "NO", "FK (creditos)", "-", "Crédito al que pertenece esta cuota (ON DELETE CASCADE)."),
        ("numero_cuota", "INTEGER", "NO", "UNIQUE(cred, num)", "-", "Número ordinal correlativo de la cuota (1, 2, 3...)."),
        ("monto_cuota", "DECIMAL(12,2)", "NO", "CHECK (> 0)", "-", "Monto programado a pagar (incluye capital e interés amortizado)."),
        ("fecha_vencimiento", "DATE", "NO", "-", "-", "Fecha límite para cancelar esta cuota específica."),
        ("estado", "VARCHAR(20)", "NO", "CHECK (PENDIENTE, PAGADA)", "'PENDIENTE'", "Estado: PENDIENTE o PAGADA (abono total recibido).")
    ]
    story.extend(create_table_doc(
        "5. Tabla: cuotas (Plan de Amortización)",
        "Entidad débil que desglosa el crédito en partes pagaderas según la frecuencia pactada.",
        cuotas_cols
    ))

    # 5. GESTIONES_COBRO
    gestiones_cols = [
        ("id", "INTEGER", "NO", "PK", "GENERATED IDENTITY", "Identificador único de la gestión de cobranza."),
        ("credito_id", "INTEGER", "NO", "FK (creditos)", "-", "Crédito objeto de la labor de cobranza en campo."),
        ("cuota_id", "INTEGER", "NO", "FK (cuotas)", "-", "Cuota específica sobre la cual se hace la gestión."),
        ("fecha_gestion", "TIMESTAMP", "NO", "-", "CURRENT_TIMESTAMP", "Momento exacto en que se realiza la interacción."),
        ("tipo_gestion", "VARCHAR(30)", "NO", "CHECK", "-", "Canal utilizado: LLAMADA, MENSAJE o VISITA."),
        ("resultado", "VARCHAR(100)", "NO", "-", "-", "Resultado: PAGO_CONFIRMADO, PROMESA_PAGO, SIN_RESPUESTA, etc."),
        ("observaciones", "TEXT", "SÍ", "-", "NULL", "Anotaciones cualitativas detalladas por el cobrador.")
    ]
    story.extend(create_table_doc(
        "6. Tabla: gestiones_cobro (Trazabilidad de Cobranza)",
        "Historial operativo que registra cada contacto entre el cobrador y el deudor, permitiendo auditar la productividad.",
        gestiones_cols
    ))

    # 6. PAGOS
    pagos_cols = [
        ("id", "INTEGER", "NO", "PK", "GENERATED IDENTITY", "Identificador único e irrepetible del pago."),
        ("cuota_id", "INTEGER", "NO", "FK (cuotas)", "-", "Cuota a la cual se abona el dinero."),
        ("gestion_cobro_id", "INTEGER", "SÍ", "FK (gestiones_cobro)", "NULL", "Gestión de cobro asociada que originó el recaudo."),
        ("monto", "DECIMAL(12,2)", "NO", "CHECK (> 0)", "-", "Valor monetario efectivamente recibido por el cobrador."),
        ("fecha_pago", "TIMESTAMP", "NO", "-", "CURRENT_TIMESTAMP", "Fecha y hora en que se asienta el pago en la app.")
    ]
    story.extend(create_table_doc(
        "7. Tabla: pagos (Recaudos y Abonos)",
        "Registra los ingresos monetarios percibidos por cuota, permitiendo abonos parciales o totales.",
        pagos_cols
    ))

    # 7. VISTAS Y OBJETOS ESPECIALES
    story.append(Paragraph("<b>8. Vistas de Base de Datos y Optimización</b>", h1_style))
    views_cols = [
        ("Nombre del Objeto", "Tipo", "Estrategia", "Propósito Funcional / Técnico"),
        ("vista_cuotas_vencidas", "VIEW (Regular)", "Consulta dinámica al vuelo", "Monitoreo en tiempo real de cuotas en mora con cálculo de días vencidos e intereses."),
        ("vista_resumen_creditos", "MATERIALIZED VIEW", "Snapshot en disco con índice único", "Consolidado financiero por crédito: calcula capital, total pagado, saldo y el flag booleano 'interes_cubierto_apto_refinanciar'."),
        ("trg_calcular_tasa_interes", "TRIGGER (PL/pgSQL)", "BEFORE INSERT OR UPDATE", "Evalúa fecha_limite - fecha_inicio y asigna automáticamente 5%, 10% o 20%.")
    ]
    v_rows = [[Paragraph(c[0], table_header_style if i==0 else cell_bold),
               Paragraph(c[1], table_header_style if i==0 else cell_style),
               Paragraph(c[2], table_header_style if i==0 else cell_style),
               Paragraph(c[3], table_header_style if i==0 else cell_style)]
              for i, c in enumerate(views_cols)]
    v_table = Table(v_rows, colWidths=[120, 90, 110, 212])
    v_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor('#1A365D')),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor('#CBD5E0')),
        ('VALIGN', (0,0), (-1,-1), 'TOP'),
        ('PADDING', (0,0), (-1,-1), 4),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor('#F7FAFC')])
    ]))
    story.append(v_table)

    # Build Document
    doc.build(story, canvasmaker=NumberedCanvas)
    print("PDF Diccionario de Datos generado exitosamente.")

if __name__ == '__main__':
    output_path = "c:/Users/Jonathan/Desktop/Universidad/Grupo de Trabajo/cobranzas/Diccionario_Datos_Cobranzas.pdf"
    build_pdf(output_path)
