# language: es
Característica: Proceso de votación en la aplicación electoral
  Para garantizar que cada ciudadano ejerza su derecho al voto una única vez
  Como votante habilitado
  Quiero seleccionar mi tipo de voto y confirmar mi elección

  Antecedentes:
    Dado que el votante está autenticado correctamente
    Y que el sistema valida si ya ha votado
    Y existen listas, partidos y candidatos cargados

  Escenario: Votante ordinario emite voto preferente por candidato
    Cuando ingreso como votante de circunscripción ordinaria
    Y selecciono un candidato específico
    Y confirmo mi voto
    Entonces el sistema registra mi voto preferente
    Y ya no puedo volver a ingresar a votar

  Escenario: Votante ordinario emite voto no preferente por una lista
    Cuando ingreso como votante de circunscripción ordinaria
    Y selecciono una lista sin candidato preferente
    Y confirmo mi voto
    Entonces el sistema registra mi voto no preferente

  Escenario: Votante ordinario emite voto en blanco
    Cuando ingreso como votante de circunscripción ordinaria
    Y selecciono la opción de voto en blanco
    Y confirmo mi voto
    Entonces el sistema registra mi voto en blanco

  Escenario: Votante indígena elige circunscripción indígena
    Cuando ingreso como votante de circunscripción indígena
    Y selecciono votar por la circunscripción indígena
    Y selecciono un candidato o lista o voto en blanco
    Y confirmo mi voto
    Entonces el sistema registra mi voto en la circunscripción indígena

  Escenario: Votante indígena elige circunscripción ordinaria
    Cuando ingreso como votante de circunscripción indígena
    Y selecciono votar por la circunscripción ordinaria
    Y selecciono un candidato o lista o voto en blanco
    Y confirmo mi voto
    Entonces el sistema registra mi voto en la circunscripción ordinaria

  Escenario: Votante indígena intenta votar en ambas circunscripciones
    Cuando ingreso como votante de circunscripción indígena
    Y selecciono votar en las dos circunscripciones
    Entonces veo el mensaje "Solo puede elegir una circunscripción"

  Escenario: Votante intenta ingresar nuevamente luego de votar
    Cuando un votante que ya emitió su voto intenta ingresar
    Entonces veo el mensaje "Usted ya ejerció su derecho al voto"
    Y no se permite el ingreso al módulo de votación
