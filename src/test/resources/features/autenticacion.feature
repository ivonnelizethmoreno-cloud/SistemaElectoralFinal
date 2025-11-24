# language: es
Característica: Autenticación de usuarios en el sistema electoral
  Para garantizar que solo usuarios válidos ingresen a la plataforma
  Como votante o administrador
  Quiero iniciar sesión según mis credenciales registradas

  Antecedentes:
    Dado que existen usuarios administradores registrados en la base de datos
    Y que los votantes válidos han sido previamente cargados por el administrador
    Y que el sistema registra los intentos de inicio de sesión

  Escenario: Autenticación exitosa de un votante registrado
    Cuando ingreso mis credenciales válidas como votante
    Entonces accedo a la aplicación en modo votante
    Y se registra el acceso exitoso en la base de datos

  Escenario: Autenticación fallida de un votante no registrado
    Cuando ingreso mis credenciales como votante no cargado en la base de datos
    Entonces veo el mensaje "Usuario no habilitado para votar"
    Y se registra el intento fallido en la base de datos

  Escenario: Autenticación exitosa de un administrador
    Cuando ingreso mis credenciales válidas como administrador
    Entonces accedo al panel de administración
    Y el sistema habilita las funciones de gestión correspondientes

  Escenario: Credenciales incorrectas para cualquier usuario
    Cuando ingreso credenciales inválidas
    Entonces veo el mensaje "Credenciales incorrectas"
    Y no se permite el ingreso al sistema
