# Rincón Sur Probadores

Aplicación móvil desarrollada en **Kotlin** con **Jetpack Compose** para gestionar los turnos y probadores de una tienda de retail. Se conecta a microservicios propios hechos en Spring Boot y consume una API externa del clima.

## Integrantes
- **Slayer248** - [@Slayer248](https://github.com/Slayer248)(los comit hechos por matias realmente los hice yo desde su computador)
- **Mapache** - [@Mapachecs](https://github.com/Mapachecs)

## Repositorios
- App móvil: https://github.com/Slayer248/RinconSurProbadores
- Microservicios: https://github.com/Slayer248/Backend_RinconSurProbadores

## Funcionalidades

**Clientes (sin cuenta)**
- Ingresan solo con su nombre y solicitan un turno indicando de 1 a 6 prendas, con opción de atención preferencial.
- Ven los probadores libres, la fila de espera y el tiempo estimado.
- Pueden cancelar su turno y reingresar a él si cierran la app.
- Reciben una notificación con vibración al llegar a las posiciones 5, 3 y 1 de la fila (con la pantalla del cliente abierta).
- En la pantalla de inicio ven el clima de Santiago con una sugerencia de qué probarse.

**Asistentes**
- Ven el estado de los probadores y la fila de espera, y cambian el estado de cada probador.
- Modifican su perfil y su contraseña.

**Administrador**
- Crea, edita, bloquea y elimina cuentas.
- Crea, bloquea y elimina probadores.
- Al eliminar una cuenta o un probador, el backend guarda una copia en un microservicio de archivo.

**Acceso**
- Inicio de sesión de empleados y recuperación de contraseña por correo.

## Arquitectura

La app usa **MVVM**:

```
Pantalla (Compose) -> ViewModel -> Repository -> Servicio Retrofit -> API
```

| Paquete | Contenido |
|---|---|
| `ui/` | Pantallas y ViewModels (`auth`, `cliente`, `asistente`, `admin`) y tema |
| `data/repository/` | Repositorios |
| `data/remote/` | Servicios y clientes Retrofit, modelos de la API y manejo de errores |
| `data/local/entities/` | Entidades que usa la app |
| `utils/` | Validaciones, sesión, notificaciones y sugerencia del clima |

**APIs nativas de Android:** notificaciones, vibración, permisos en tiempo de ejecución, `SharedPreferences` y `Toast`.

## Endpoints utilizados

### API externa: Open-Meteo

| Método | URL | Uso |
|---|---|---|
| GET | `https://api.open-meteo.com/v1/forecast?latitude=-33.45&longitude=-70.66&current=temperature_2m` | Temperatura actual de Santiago |

No requiere clave para uso no comercial. La app la consulta directamente con Retrofit (`ClimaApi` y `ClimaClient`), sin pasar por los microservicios. Si falla, muestra "Clima no disponible" y el resto de la app sigue funcionando. Datos de [Open-Meteo.com](https://open-meteo.com/).

| Temperatura | Sugerencia |
|---|---|
| Menos de 15 °C | Probarse un abrigo |
| De 15 a 24 °C | Probarse algo cómodo |
| Más de 24 °C | Probarse ropa ligera |

### Microservicios propios

La app se comunica solo con el **gateway** (puerto 8090), que envía cada solicitud al microservicio correspondiente.

| Servicio | Método | Ruta |
|---|---|---|
| Roles | GET | `/api/roles` |
| Usuarios | POST | `/api/usuarios/login` |
| Usuarios | POST | `/api/usuarios/recuperar` |
| Usuarios | GET | `/api/usuarios` |
| Usuarios | GET | `/api/usuarios/{id}` |
| Usuarios | GET | `/api/usuarios/username/{username}` |
| Usuarios | POST | `/api/usuarios` |
| Usuarios | PUT | `/api/usuarios/{id}` |
| Usuarios | PUT | `/api/usuarios/{id}/contrasena` |
| Usuarios | PATCH | `/api/usuarios/{id}/bloqueo` |
| Usuarios | DELETE | `/api/usuarios/{id}` |
| Probadores | GET | `/api/probadores` |
| Probadores | GET | `/api/probadores/{codigo}` |
| Probadores | POST | `/api/probadores` |
| Probadores | PUT | `/api/probadores/{codigo}/estado` |
| Probadores | PATCH | `/api/probadores/{codigo}/bloqueo` |
| Probadores | DELETE | `/api/probadores/{codigo}` |
| Turnos | GET | `/api/turnos` |
| Turnos | GET | `/api/turnos/{id}` |
| Turnos | POST | `/api/turnos` |
| Turnos | POST | `/api/turnos/atender-siguiente` |
| Turnos | PATCH | `/api/turnos/{id}/estado` |

## Cómo ejecutar el proyecto

### Requisitos
- Android Studio y un emulador o dispositivo con Android 7.0 (API 24) o superior.
- Java 21 y MySQL en `localhost:3306` (para los microservicios).
- Conexión a internet (para las dependencias y el clima).

### 1. Levantar los microservicios
1. Clona el repositorio del backend:
   ```
   git clone https://github.com/Slayer248/Backend_RinconSurProbadores.git
   ```
2. Inicia MySQL.
3. Abre la carpeta en VS Code, ve a **Ejecución y depuración**, elige **Backend completo** y presiona ▶.
4. Comprueba en `http://localhost:8761` que los servicios estén registrados.

Los detalles (puertos, correo y Swagger) están en el README del backend.

### 2. Ejecutar la app
1. Clona este repositorio:
   ```
   git clone https://github.com/Slayer248/RinconSurProbadores.git
   ```
2. En Android Studio selecciona **Open** y elige la carpeta `RinconSurProbadores`.
3. Espera a que Gradle sincronice las dependencias.
4. Revisa la dirección del backend (ver la sección siguiente).
5. Inicia un emulador, o conecta un dispositivo con depuración USB, y presiona **Run** (Shift + F10).

### Dirección del backend
Se configura en `BASE_URL`, dentro de `data/remote/RetrofitClient.kt`:

| Dónde corre la app | Valor |
|---|---|
| Emulador en el mismo PC que el backend | `http://10.0.2.2:8090/` |
| Otro PC o un celular en la misma red | `http://<IP del PC del backend>:8090/` |

En el segundo caso, el firewall del PC del backend debe permitir el puerto 8090.

## Pruebas unitarias

Usan **JUnit 4** y no necesitan el backend ni internet. En Windows:

```
.\gradlew.bat testDebugUnitTest
```

| Archivo | Qué prueba |
|---|---|
| `ModelosTest` | Texto de "Último uso" de un probador |
| `SolicitudesTest` | Datos que se envían al crear, editar y cambiar contraseña |
| `NetworkHelperTest` | Respuestas correctas y errores de la API |
| `ClimaRepositoryTest` | Repositorio del clima con la API externa simulada |
| `PasswordValidatorTest` | Reglas de una contraseña segura |
| `SugerenciaClimaTest` | Sugerencia según la temperatura |

## APK firmado

_Pendiente: agregar la captura del APK firmado y del archivo `.jks`, y los pasos de generación._
