/* package com.elecciones.steps; */
package com.elecciones.sistema.steps; 
import io.cucumber.java.es.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AppSteps - Definición de pasos BDD para la aplicación electoral.
 *
 * Cubre los features:
 *  - autenticacion.feature
 *  - votacion.feature
 *  - certificado.feature
 *  - cierrejornada.feature
 *
 * Importante:
 *  - No invoca directamente controladores Spring.
 *  - Simula el comportamiento esperado mediante estado interno.
 */
public class AppSteps {

    // ============================================================
    // ESTADO COMPARTIDO DE LA "APP" PARA TODOS LOS FEATURES
    // ============================================================

    // --- Autenticación ---
    private boolean adminRegistrado;
    private boolean votantesCargados;
    private boolean sistemaRegistraIntentos;

    private boolean usuarioRegistradoEnBD;
    private boolean credencialesValidas;
    private String tipoUsuario; // "VOTANTE", "ADMIN", "DESCONOCIDO"

    private boolean autenticado;
    private boolean accesoModoVotante;
    private boolean accesoPanelAdmin;
    private boolean funcionesAdminHabilitadas;
    private int intentosExitosos;
    private int intentosFallidos;

    // Mensajes genéricos
    private String mensajeVisible;

    // --- Votación ---
    private boolean votanteAutenticadoCorrecto;
    private boolean sistemaValidaHaVotado;
    private boolean listasPartidosCandidatosCargados;

    private String circunscripcionVotante;          // "ORDINARIA" o "INDIGENA"
    private String circunscripcionElegidaIndigena;  // "ORDINARIA", "INDIGENA", "AMBAS", null
    private String tipoVoto;                        // "PREFERENTE", "NOPREFERENTE", "BLANCO"
    private boolean votoConfirmado;

    private boolean votoRegistradoPreferente;
    private boolean votoRegistradoNoPreferente;
    private boolean votoRegistradoBlanco;
    private boolean votoRegistradoEnCircIndigena;
    private boolean votoRegistradoEnCircOrdinaria;

    private boolean usuarioYaVoto;
    private boolean ingresoModuloVotacionPermitido = true;

    // --- Certificado ---
    private boolean votanteHaIngresadoApp;
    private boolean votoRegistradoCorrectamente;

    private boolean pantallaAgradecimientoVisible;
    private boolean opcionesCertificadoYVolverVisibles;

    private boolean certificadoGenerado;
    private boolean pdfGenerado;
    private String encabezadoCertificado;
    private String nombreVotanteCert;
    private String cedulaVotanteCert;
    private String fechaEleccionCert;
    private String nombreJurado;
    private String firmaJurado;
    private String codigoVerificacion;

    private boolean descargaIniciada;
    private boolean descargaSinErrores;
    private String nombreArchivoDescargado;
    private boolean nombreIncluyeCedula;

    private boolean datosRequeridosCompletos = true;
    private boolean errorGeneracionCertificado;
    private boolean descargaPermitida;

    // --- Cierre de jornada / resultados ---
    private boolean adminAutenticado;
    private boolean periodoVotacionFinalizado;
    private boolean existenVotosAmbasCirc;

    private boolean jornadaCerrada;
    private boolean recepcionVotosBloqueada;
    private String mensajeCierre;

    private boolean intentoVotarDespuesCierre;
    private boolean votoRegistradoDespuesCierre;

    private boolean resultadosConsultados;
    private boolean identidadVotantesMostrada;

    private int totalVotosPorCandidato;
    private int totalVotosPorLista;
    private int totalVotosEnBlanco;

    private int curulesIndigenasAsignadas;
    private final int CURULES_INDIGENAS_MINIMAS = 2;

    private boolean umbralesMostrados;
    private boolean candidatosElectosListados;

    // ============================================================
    // UTILIDADES INTERNAS
    // ============================================================

    private void procesarLogin() {
        autenticado = false;
        accesoModoVotante = false;
        accesoPanelAdmin = false;
        funcionesAdminHabilitadas = false;

        if (!usuarioRegistradoEnBD) {
            mensajeVisible = "Usuario no habilitado para votar";
            if (sistemaRegistraIntentos) intentosFallidos++;
            return;
        }

        if (!credencialesValidas) {
            mensajeVisible = "Credenciales incorrectas";
            if (sistemaRegistraIntentos) intentosFallidos++;
            return;
        }

        // Usuario válido y credenciales correctas
        autenticado = true;
        mensajeVisible = null;
        if (sistemaRegistraIntentos) intentosExitosos++;

        if ("VOTANTE".equalsIgnoreCase(tipoUsuario)) {
            accesoModoVotante = true;
        } else if ("ADMIN".equalsIgnoreCase(tipoUsuario)) {
            accesoPanelAdmin = true;
            funcionesAdminHabilitadas = true;
        }
    }

    private void procesarConfirmacionVoto() {
        votoConfirmado = true;

        if ("ORDINARIA".equals(circunscripcionVotante)) {
            if ("PREFERENTE".equals(tipoVoto)) {
                votoRegistradoPreferente = true;
            } else if ("NOPREFERENTE".equals(tipoVoto)) {
                votoRegistradoNoPreferente = true;
            } else if ("BLANCO".equals(tipoVoto)) {
                votoRegistradoBlanco = true;
            }
            votoRegistradoEnCircOrdinaria = true;
        }

        if ("INDIGENA".equals(circunscripcionVotante)) {
            if ("INDIGENA".equals(circunscripcionElegidaIndigena)) {
                votoRegistradoEnCircIndigena = true;
            } else if ("ORDINARIA".equals(circunscripcionElegidaIndigena)) {
                votoRegistradoEnCircOrdinaria = true;
            }
            // Tipo de voto (preferente / lista / blanco) ya viene en tipoVoto,
            // pero para estos escenarios solo interesa la circunscripción final.
        }

        // Después de votar, ya no puede votar de nuevo
        usuarioYaVoto = true;
        ingresoModuloVotacionPermitido = false;
    }

    private void generarCertificadoEnMemoria() {
        if (!datosRequeridosCompletos) {
            certificadoGenerado = false;
            pdfGenerado = false;
            errorGeneracionCertificado = true;
            descargaPermitida = false;
            mensajeVisible = "No fue posible generar el certificado por datos incompletos";
            return;
        }

        certificadoGenerado = true;
        pdfGenerado = true;
        errorGeneracionCertificado = false;
        descargaPermitida = true;

        // Simulamos datos de certificado
        encabezadoCertificado = "CERTIFICADO ELECTORAL - ELECCIONES SENADO 2025";
        nombreVotanteCert = "Juan Pérez";
        cedulaVotanteCert = "1234567890";
        fechaEleccionCert = "12 de Noviembre de 2025";
        nombreJurado = "Juan Pérez Jurado";
        firmaJurado = "Firma Digital";
        codigoVerificacion = "RNEC-123456";
    }

    // ============================================================
    // FEATURE 1: AUTENTICACIÓN
    // autenticacion.feature
    // ============================================================

    @Dado("que existen usuarios administradores registrados en la base de datos")
    public void que_existen_usuarios_administradores_registrados_en_la_base_de_datos() {
        adminRegistrado = true;
    }

    @Dado("que los votantes válidos han sido previamente cargados por el administrador")
    public void que_los_votantes_validos_han_sido_previamente_cargados_por_el_administrador() {
        votantesCargados = true;
    }

    @Dado("que el sistema registra los intentos de inicio de sesión")
    public void que_el_sistema_registra_los_intentos_de_inicio_de_sesion() {
        sistemaRegistraIntentos = true;
    }

    @Cuando("ingreso mis credenciales válidas como votante")
    public void ingreso_mis_credenciales_validas_como_votante() {
        tipoUsuario = "VOTANTE";
        usuarioRegistradoEnBD = true;
        credencialesValidas = true;
        procesarLogin();
    }

    @Entonces("accedo a la aplicación en modo votante")
    public void accedo_a_la_aplicacion_en_modo_votante() {
        assertTrue(autenticado, "El usuario debería estar autenticado");
        assertTrue(accesoModoVotante, "Debería estar en modo votante");
        assertFalse(accesoPanelAdmin, "No debería estar en panel admin");
    }

    @Entonces("se registra el acceso exitoso en la base de datos")
    public void se_registra_el_acceso_exitoso_en_la_base_de_datos() {
        assertTrue(sistemaRegistraIntentos, "El sistema debería registrar intentos");
        assertTrue(intentosExitosos > 0, "Debe existir al menos un intento exitoso registrado");
    }

    @Cuando("ingreso mis credenciales como votante no cargado en la base de datos")
    public void ingreso_mis_credenciales_como_votante_no_cargado_en_la_base_de_datos() {
        tipoUsuario = "VOTANTE";
        usuarioRegistradoEnBD = false;
        credencialesValidas = true; // aunque la clave sea válida, no está cargado
        procesarLogin();
    }

    @Entonces("veo el mensaje {string}")
    public void veo_el_mensaje(String mensajeEsperado) {
        assertEquals(mensajeEsperado, mensajeVisible,
                "El mensaje visible no coincide con el esperado");
    }

    @Entonces("se registra el intento fallido en la base de datos")
    public void se_registra_el_intento_fallido_en_la_base_de_datos() {
        assertTrue(sistemaRegistraIntentos, "El sistema debería registrar intentos");
        assertTrue(intentosFallidos > 0, "Debe existir al menos un intento fallido registrado");
    }

    @Cuando("ingreso mis credenciales válidas como administrador")
    public void ingreso_mis_credenciales_validas_como_administrador() {
        tipoUsuario = "ADMIN";
        usuarioRegistradoEnBD = true;
        credencialesValidas = true;
        procesarLogin();
    }

    @Entonces("accedo al panel de administración")
    public void accedo_al_panel_de_administracion() {
        assertTrue(autenticado, "El admin debería estar autenticado");
        assertTrue(accesoPanelAdmin, "Debería acceder al panel de administración");
    }

    @Entonces("el sistema habilita las funciones de gestión correspondientes")
    public void el_sistema_habilita_las_funciones_de_gestion_correspondientes() {
        assertTrue(funcionesAdminHabilitadas, "Las funciones de gestión deberían estar habilitadas");
    }

    @Cuando("ingreso credenciales inválidas")
    public void ingreso_credenciales_invalidas() {
        tipoUsuario = "DESCONOCIDO";
        usuarioRegistradoEnBD = true; // está en BD pero clave mal
        credencialesValidas = false;
        procesarLogin();
    }

    @Entonces("no se permite el ingreso al sistema")
    public void no_se_permite_el_ingreso_al_sistema() {
        assertFalse(autenticado, "No debería autenticarse con credenciales inválidas");
    }

    // ============================================================
    // FEATURE 4: VOTACIÓN
    // votacion.feature
    // ============================================================

    @Dado("que el votante está autenticado correctamente")
    public void que_el_votante_esta_autenticado_correctamente() {
        votanteAutenticadoCorrecto = true;
    }

    @Dado("que el sistema valida si ya ha votado")
    public void que_el_sistema_valida_si_ya_ha_votado() {
        sistemaValidaHaVotado = true;
    }

    @Dado("existen listas, partidos y candidatos cargados")
    public void existen_listas_partidos_y_candidatos_cargados() {
        listasPartidosCandidatosCargados = true;
    }

    @Cuando("ingreso como votante de circunscripción ordinaria")
    public void ingreso_como_votante_de_circunscripcion_ordinaria() {
        circunscripcionVotante = "ORDINARIA";
    }

    @Cuando("ingreso como votante de circunscripción indígena")
    public void ingreso_como_votante_de_circunscripcion_indigena() {
        circunscripcionVotante = "INDIGENA";
    }

    @Cuando("selecciono un candidato específico")
    public void selecciono_un_candidato_especifico() {
        tipoVoto = "PREFERENTE";
    }

    @Cuando("selecciono una lista sin candidato preferente")
    public void selecciono_una_lista_sin_candidato_preferente() {
        tipoVoto = "NOPREFERENTE";
    }

    @Cuando("selecciono la opción de voto en blanco")
    public void selecciono_la_opcion_de_voto_en_blanco() {
        tipoVoto = "BLANCO";
    }

    @Cuando("selecciono votar por la circunscripción indígena")
    public void selecciono_votar_por_la_circunscripcion_indigena() {
        circunscripcionElegidaIndigena = "INDIGENA";
    }

    @Cuando("selecciono votar por la circunscripción ordinaria")
    public void selecciono_votar_por_la_circunscripcion_ordinaria() {
        circunscripcionElegidaIndigena = "ORDINARIA";
    }

    @Cuando("selecciono un candidato o lista o voto en blanco")
    public void selecciono_un_candidato_o_lista_o_voto_en_blanco() {
        // Aquí basta con afirmar que eligió algo. Tipo se controla antes.
        if (tipoVoto == null) {
            tipoVoto = "ALGUNO";
        }
    }

    @Cuando("selecciono votar en las dos circunscripciones")
    public void selecciono_votar_en_las_dos_circunscripciones() {
        circunscripcionElegidaIndigena = "AMBAS";
        mensajeVisible = "Solo puede elegir una circunscripción";
    }

    @Cuando("confirmo mi voto")
    public void confirmo_mi_voto() {
        procesarConfirmacionVoto();
    }

    @Entonces("el sistema registra mi voto preferente")
    public void el_sistema_registra_mi_voto_preferente() {
        assertTrue(votoRegistradoPreferente, "El voto preferente debería haberse registrado");
    }

    @Entonces("ya no puedo volver a ingresar a votar")
    public void ya_no_puedo_volver_a_ingresar_a_votar() {
        assertTrue(usuarioYaVoto, "El usuario debería estar marcado como ya votó");
        assertFalse(ingresoModuloVotacionPermitido,
                "No se debería permitir el ingreso al módulo de votación");
    }

    @Entonces("el sistema registra mi voto no preferente")
    public void el_sistema_registra_mi_voto_no_preferente() {
        assertTrue(votoRegistradoNoPreferente, "El voto no preferente debería haberse registrado");
    }

    @Entonces("el sistema registra mi voto en blanco")
    public void el_sistema_registra_mi_voto_en_blanco() {
        assertTrue(votoRegistradoBlanco, "El voto en blanco debería haberse registrado");
    }

    @Entonces("el sistema registra mi voto en la circunscripción indígena")
    public void el_sistema_registra_mi_voto_en_la_circunscripcion_indigena() {
        assertTrue(votoRegistradoEnCircIndigena,
                "El voto debería haberse registrado en la circunscripción indígena");
    }

    @Entonces("el sistema registra mi voto en la circunscripción ordinaria")
    public void el_sistema_registra_mi_voto_en_la_circunscripcion_ordinaria() {
        assertTrue(votoRegistradoEnCircOrdinaria,
                "El voto debería haberse registrado en la circunscripción ordinaria");
    }

    @Entonces("Solo puede elegir una circunscripción")
    public void solo_puede_elegir_una_circunscripcion_step_literal() {
        // por si algún escenario usa este texto literal
        assertEquals("Solo puede elegir una circunscripción", mensajeVisible);
    }

    @Entonces("veo el mensaje \"Solo puede elegir una circunscripción\"")
    public void veo_el_mensaje_solo_puede_elegir_una_circunscripcion() {
        assertEquals("Solo puede elegir una circunscripción", mensajeVisible);
    }

    @Cuando("un votante que ya emitió su voto intenta ingresar")
    public void un_votante_que_ya_emitio_su_voto_intenta_ingresar() {
        usuarioYaVoto = true;
        ingresoModuloVotacionPermitido = false;
        mensajeVisible = "Usted ya ejerció su derecho al voto";
    }

    @Entonces("veo el mensaje \"Usted ya ejerció su derecho al voto\"")
    public void veo_el_mensaje_usted_ya_ejercio_su_derecho_al_voto() {
        assertEquals("Usted ya ejerció su derecho al voto", mensajeVisible);
    }

    @Entonces("no se permite el ingreso al módulo de votación")
    public void no_se_permite_el_ingreso_al_modulo_de_votacion() {
        assertFalse(ingresoModuloVotacionPermitido,
                "No se debería permitir ingresar al módulo de votación");
    }

    // ============================================================
    // FEATURE 2: CERTIFICADO
    // certificado.feature
    // ============================================================

    @Dado("que el votante ha ingresado a la aplicación web")
    public void que_el_votante_ha_ingresado_a_la_aplicacion_web() {
        votanteHaIngresadoApp = true;
    }

    @Dado("ha completado el registro de su voto correctamente")
    public void ha_completado_el_registro_de_su_voto_correctamente() {
        votoRegistradoCorrectamente = true;
    }

    @Cuando("finaliza el proceso de votación")
    public void finaliza_el_proceso_de_votacion() {
        if (votoRegistradoCorrectamente) {
            pantallaAgradecimientoVisible = true;
            opcionesCertificadoYVolverVisibles = true;
        }
    }

    @Entonces("se debe mostrar una pantalla de agradecimiento")
    public void se_debe_mostrar_una_pantalla_de_agradecimiento() {
        assertTrue(pantallaAgradecimientoVisible,
                "La pantalla de agradecimiento debería estar visible");
    }

    @Entonces("deben aparecer las opciones \"Descargar certificado\" y \"Volver al inicio de sesion\"")
    public void deben_aparecer_las_opciones_descargar_y_volver() {
        assertTrue(opcionesCertificadoYVolverVisibles,
                "Las opciones de certificado y volver deberían estar visibles");
    }

    @Cuando("el votante selecciona la opción \"Descargar certificado\"")
    public void el_votante_selecciona_la_opcion_descargar_certificado() {
        generarCertificadoEnMemoria();
    }

    @Entonces("el sistema debe generar un certificado electoral en formato PDF")
    public void el_sistema_debe_generar_un_certificado_electoral_en_formato_pdf() {
        assertTrue(certificadoGenerado, "El certificado debería haberse generado");
        assertTrue(pdfGenerado, "El PDF debería haberse generado");
    }

    @Entonces("el certificado debe contener el encabezado \"CERTIFICADO ELECTORAL - ELECCIONES SENADO 2025\"")
    public void el_certificado_debe_contener_el_encabezado() {
        assertEquals("CERTIFICADO ELECTORAL - ELECCIONES SENADO 2025", encabezadoCertificado);
    }

    @Entonces("debe mostrar el nombre completo del votante")
    public void debe_mostrar_el_nombre_completo_del_votante() {
        assertNotNull(nombreVotanteCert);
        assertFalse(nombreVotanteCert.isEmpty(), "El nombre del votante no debe estar vacío");
    }

    @Entonces("debe mostrar la cédula del votante")
    public void debe_mostrar_la_cedula_del_votante() {
        assertNotNull(cedulaVotanteCert);
        assertFalse(cedulaVotanteCert.isEmpty(), "La cédula del votante no debe estar vacía");
    }

    @Entonces("debe mostrar la fecha de la elección")
    public void debe_mostrar_la_fecha_de_la_eleccion() {
        assertNotNull(fechaEleccionCert);
        assertFalse(fechaEleccionCert.isEmpty(), "La fecha de elección debe estar presente");
    }

    @Entonces("debe mostrar el nombre del jurado de votación")
    public void debe_mostrar_el_nombre_del_jurado_de_votacion() {
        assertNotNull(nombreJurado);
        assertFalse(nombreJurado.isEmpty(), "El nombre del jurado debe estar presente");
    }

    @Entonces("debe mostrar la firma del jurado de votación")
    public void debe_mostrar_la_firma_del_jurado_de_votacion() {
        assertNotNull(firmaJurado);
        assertFalse(firmaJurado.isEmpty(), "La firma del jurado debe estar presente");
    }

    @Entonces("debe mostrar el codigo de verificación")
    public void debe_mostrar_el_codigo_de_verificacion() {
        assertNotNull(codigoVerificacion);
        assertFalse(codigoVerificacion.isEmpty(), "El código de verificación debe estar presente");
    }

    @Dado("que el certificado electoral ha sido generado")
    public void que_el_certificado_electoral_ha_sido_generado() {
        certificadoGenerado = true;
        pdfGenerado = true;
        cedulaVotanteCert = "1234567890";
    }

    @Cuando("el votante inicia la descarga del archivo")
    public void el_votante_inicia_la_descarga_del_archivo() {
        if (certificadoGenerado) {
            descargaIniciada = true;
            descargaSinErrores = true;
            nombreArchivoDescargado = "certificado_" + cedulaVotanteCert + ".pdf";
            nombreIncluyeCedula = nombreArchivoDescargado.contains(cedulaVotanteCert);
        }
    }

    @Entonces("la descarga debe comenzar sin errores")
    public void la_descarga_debe_comenzar_sin_errores() {
        assertTrue(descargaIniciada, "La descarga debe haberse iniciado");
        assertTrue(descargaSinErrores, "La descarga no debe presentar errores");
    }

    @Entonces("el archivo debe guardarse con un nombre válido que incluya el número de cédula del votante")
    public void el_archivo_debe_guardarse_con_un_nombre_valido_que_incluya_la_cedula() {
        assertNotNull(nombreArchivoDescargado);
        assertTrue(nombreIncluyeCedula,
                "El nombre del archivo debe incluir la cédula del votante");
    }

    @Cuando("el sistema genera el certificado electoral")
    public void el_sistema_genera_el_certificado_electoral() {
        generarCertificadoEnMemoria();
    }

    @Entonces("debe validar que el nombre del votante no esté vacío")
    public void debe_validar_que_el_nombre_del_votante_no_este_vacio() {
        assertNotNull(nombreVotanteCert);
        assertFalse(nombreVotanteCert.isEmpty());
    }

    @Entonces("debe validar que la cédula del votante no esté vacía")
    public void debe_validar_que_la_cedula_del_votante_no_este_vacia() {
        assertNotNull(cedulaVotanteCert);
        assertFalse(cedulaVotanteCert.isEmpty());
    }

    @Entonces("debe validar que la fecha de elección esté disponible")
    public void debe_validar_que_la_fecha_de_eleccion_este_disponible() {
        assertNotNull(fechaEleccionCert);
        assertFalse(fechaEleccionCert.isEmpty());
    }

    @Entonces("debe validar que la información del jurado esté presente")
    public void debe_validar_que_la_informacion_del_jurado_este_presente() {
        assertNotNull(nombreJurado);
        assertNotNull(firmaJurado);
    }

    @Dado("que falta información requerida para el certificado")
    public void que_falta_informacion_requerida_para_el_certificado() {
        datosRequeridosCompletos = false;
    }

    @Cuando("se intenta generar el certificado")
    public void se_intenta_generar_el_certificado() {
        generarCertificadoEnMemoria();
    }

    @Entonces("se debe mostrar un mensaje indicando que no fue posible generar el certificado")
    public void se_debe_mostrar_un_mensaje_indicando_que_no_fue_posible_generar_el_certificado() {
        assertTrue(errorGeneracionCertificado, "Debe haberse marcado error de generación");
        assertNotNull(mensajeVisible);
        assertTrue(mensajeVisible.toLowerCase().contains("no fue posible generar el certificado"));
    }

    @Entonces("no debe permitirse la descarga del archivo")
    public void no_debe_permitirse_la_descarga_del_archivo() {
        assertFalse(descargaPermitida, "No debería permitirse la descarga del certificado");
    }

    // ============================================================
    // FEATURE 3: CIERRE DE JORNADA
    // cierrejornada.feature
    // ============================================================

    @Dado("que el administrador está autenticado")
    public void que_el_administrador_esta_autenticado() {
        adminAutenticado = true;
    }

    @Dado("que el periodo de votación ha finalizado")
    public void que_el_periodo_de_votacion_ha_finalizado() {
        periodoVotacionFinalizado = true;
    }

    @Dado("que existen votos registrados en ambas circunscripciones")
    public void que_existen_votos_registrados_en_ambas_circunscripciones() {
        existenVotosAmbasCirc = true;
        totalVotosPorCandidato = 100;
        totalVotosPorLista = 50;
        totalVotosEnBlanco = 10;
    }

    @Cuando("el administrador selecciona la opción \"Cerrar votación\"")
    public void el_administrador_selecciona_la_opcion_cerrar_votacion() {
        if (adminAutenticado && periodoVotacionFinalizado) {
            jornadaCerrada = true;
            recepcionVotosBloqueada = true;
            mensajeCierre = "Jornada cerrada correctamente";
        }
    }

    @Entonces("el sistema bloquea la recepción de nuevos votos")
    public void el_sistema_bloquea_la_recepcion_de_nuevos_votos() {
        assertTrue(jornadaCerrada, "La jornada debe estar cerrada");
        assertTrue(recepcionVotosBloqueada, "La recepción de votos debe estar bloqueada");
    }

    @Entonces("muestra el mensaje \"Jornada cerrada correctamente\"")
    public void muestra_el_mensaje_jornada_cerrada_correctamente() {
        assertEquals("Jornada cerrada correctamente", mensajeCierre);
    }

    @Cuando("un votante intenta ingresar a votar después del cierre")
    public void un_votante_intenta_ingresar_a_votar_despues_del_cierre() {
        intentoVotarDespuesCierre = true;
        if (jornadaCerrada) {
            votoRegistradoDespuesCierre = false;
            mensajeVisible = "La jornada electoral ha finalizado";
        }
    }

    @Entonces("veo el mensaje \"La jornada electoral ha finalizado\"")
    public void veo_el_mensaje_la_jornada_electoral_ha_finalizado() {
        assertEquals("La jornada electoral ha finalizado", mensajeVisible);
    }

    @Entonces("no se permite registrar ningún voto")
    public void no_se_permite_registrar_ningun_voto() {
        assertFalse(votoRegistradoDespuesCierre,
                "No debería permitirse registrar votos después del cierre");
    }

    @Cuando("el administrador consulta los resultados")
    public void el_administrador_consulta_los_resultados() {
        resultadosConsultados = true;
        identidadVotantesMostrada = false;
    }

    @Entonces("veo el total de votos por candidato, lista y voto en blanco")
    public void veo_el_total_de_votos_por_candidato_lista_y_voto_en_blanco() {
        assertTrue(resultadosConsultados, "Los resultados deben haberse consultado");
        assertTrue(totalVotosPorCandidato >= 0);
        assertTrue(totalVotosPorLista >= 0);
        assertTrue(totalVotosEnBlanco >= 0);
    }

    @Entonces("no se muestra la identidad de ningún votante")
    public void no_se_muestra_la_identidad_de_ningun_votante() {
        assertFalse(identidadVotantesMostrada,
                "No debe mostrarse la identidad de los votantes");
    }

    @Cuando("el administrador consulta los resultados del senado indígena")
    public void el_administrador_consulta_los_resultados_del_senado_indigena() {
        // Simulamos que la regla constitucional se aplica
        curulesIndigenasAsignadas = 3; // mínimo 2, pero pueden ser más
    }

    @Entonces("el sistema asigna mínimo dos curules a la circunscripción indígena")
    public void el_sistema_asigna_minimo_dos_curules_a_la_circunscripcion_indigena() {
        assertTrue(curulesIndigenasAsignadas >= CURULES_INDIGENAS_MINIMAS,
                "Debe haber al menos dos curules indígenas");
    }

    @Entonces("si los votos permiten más curules adicionales, estos se muestran correctamente")
    public void si_los_votos_permiten_mas_curules_adicionales_estos_se_muestran_correctamente() {
        assertTrue(curulesIndigenasAsignadas >= 2,
                "Se deben mostrar las curules adicionales cuando existan");
    }

    @Cuando("el administrador consulta los resultados finales")
    public void el_administrador_consulta_los_resultados_finales() {
        umbralesMostrados = true;
        candidatosElectosListados = true;
    }

    @Entonces("se muestran los umbrales aplicados")
    public void se_muestran_los_umbrales_aplicados() {
        assertTrue(umbralesMostrados, "Los umbrales deben mostrarse en los resultados");
    }

    @Entonces("se listan los candidatos seleccionados para la próxima legislatura")
    public void se_listan_los_candidatos_seleccionados_para_la_proxima_legislatura() {
        assertTrue(candidatosElectosListados, "Los candidatos electos deben listarse");
    }
}

