# language: es

Caracteristica: Generación del certificado electoral
  Como votante
  Quiero descargar un certificado electoral después de registrar mi voto
  Para tener constancia oficial de mi participación en las Elecciones Senado 2025

  Antecedentes:
    Dado que el votante ha ingresado a la aplicación web
    Y ha completado el registro de su voto correctamente

  Escenario: Mostrar opciones después de registrar el voto
    Cuando finaliza el proceso de votación
    Entonces se debe mostrar una pantalla de agradecimiento
    Y deben aparecer las opciones "Descargar certificado" y "Volver al inicio de sesion"

  Escenario: Generación correcta del certificado electoral
    Cuando el votante selecciona la opción "Descargar certificado"
    Entonces el sistema debe generar un certificado electoral en formato PDF
    Y el certificado debe contener el encabezado "CERTIFICADO ELECTORAL - ELECCIONES SENADO 2025"
    Y debe mostrar el nombre completo del votante
    Y debe mostrar la cédula del votante
    Y debe mostrar la fecha de la elección
    Y debe mostrar el nombre del jurado de votación
    Y debe mostrar la firma del jurado de votación
    Y debe mostrar el codigo de verificación

  Escenario: Descarga exitosa del certificado electoral
    Dado que el certificado electoral ha sido generado
    Cuando el votante inicia la descarga del archivo
    Entonces la descarga debe comenzar sin errores
    Y el archivo debe guardarse con un nombre válido que incluya el número de cédula del votante

  Escenario: Validación de datos requeridos para la generación del certificado
    Cuando el sistema genera el certificado electoral
    Entonces debe validar que el nombre del votante no esté vacío
    Y debe validar que la cédula del votante no esté vacía
    Y debe validar que la fecha de elección esté disponible
    Y debe validar que la información del jurado esté presente

  Escenario: Error al generar el certificado por datos faltantes
    Dado que falta información requerida para el certificado
    Cuando se intenta generar el certificado
    Entonces se debe mostrar un mensaje indicando que no fue posible generar el certificado
    Y no debe permitirse la descarga del archivo
