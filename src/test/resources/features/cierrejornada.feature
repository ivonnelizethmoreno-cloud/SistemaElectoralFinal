# language: es
Característica: Cierre de jornada electoral y consulta de resultados
  Para garantizar la transparencia y el cumplimiento del proceso electoral
  Como administrador
  Quiero cerrar la jornada y ver los resultados finales

  Antecedentes:
    Dado que el administrador está autenticado
    Y que el periodo de votación ha finalizado
    Y que existen votos registrados en ambas circunscripciones

  Escenario: Cerrar la jornada electoral
    Cuando el administrador selecciona la opción "Cerrar votación"
    Entonces el sistema bloquea la recepción de nuevos votos
    Y muestra el mensaje "Jornada cerrada correctamente"

  Escenario: Intento de votar después del cierre
    Cuando un votante intenta ingresar a votar después del cierre
    Entonces veo el mensaje "La jornada electoral ha finalizado"
    Y no se permite registrar ningún voto

  Escenario: Visualización de resultados anonimizados
    Cuando el administrador consulta los resultados
    Entonces veo el total de votos por candidato, lista y voto en blanco
    Y no se muestra la identidad de ningún votante

  Escenario: Asignación de curules con mínimo constitucional indígena
    Cuando el administrador consulta los resultados del senado indígena
    Entonces el sistema asigna mínimo dos curules a la circunscripción indígena
    Y si los votos permiten más curules adicionales, estos se muestran correctamente

  Escenario: Visualización de umbrales y candidatos electos
    Cuando el administrador consulta los resultados finales
    Entonces se muestran los umbrales aplicados
    Y se listan los candidatos seleccionados para la próxima legislatura
