# Documentación Completa de la Interfaz de Usuario (UI)

## Índice
1. [Tecnologías y Frameworks Utilizados](#tecnologías-y-frameworks-utilizados)
2. [Arquitectura General de la UI](#arquitectura-general-de-la-ui)
3. [Estructura de Componentes](#estructura-de-componentes)
4. [Flujo de Navegación](#flujo-de-navegación)
5. [Componentes Detallados](#componentes-detallados)
6. [Sistema de Comunicación Cliente-Servidor](#sistema-de-comunicación-cliente-servidor)
7. [Gestión de Hilos y Concurrencia](#gestión-de-hilos-y-concurrencia)
8. [Patrones de Diseño Implementados](#patrones-de-diseño-implementados)

---

## Tecnologías y Frameworks Utilizados

### Java Swing
La aplicación utiliza **Java Swing** como framework principal para la construcción de la interfaz gráfica de usuario (GUI). Swing es parte de la biblioteca estándar de Java y proporciona:

- **Componentes visuales**: `JFrame`, `JPanel`, `JButton`, `JTextField`, `JList`, `JScrollPane`, etc.
- **Layout Managers**: `BorderLayout`, `FlowLayout`, `GridLayout`, `GridBagLayout`, `BoxLayout`
- **Gestión de eventos**: `ActionListener`, `MouseListener`, `ListSelectionListener`, `WindowListener`
- **Diálogos**: `JOptionPane` para mensajes y confirmaciones
- **Contenedores avanzados**: `JTabbedPane`, `JSplitPane` para organizar componentes

### Características de Swing Utilizadas
- **Thread Safety**: Uso de `SwingUtilities.invokeLater()` para actualizar la UI desde hilos secundarios
- **Model-View**: Separación entre modelos de datos (`DefaultListModel`) y componentes visuales (`JList`)
- **Look and Feel**: Utiliza el look and feel por defecto del sistema operativo

---

## Arquitectura General de la UI

La aplicación sigue una arquitectura **modular y jerárquica** organizada en paquetes:

```
ui/
├── autenticacion/          # Sistema de login y registro
├── conversacion/           # Ventanas principales de chat
│   └── componentes/       # Componentes reutilizables
├── gestion_amigos/        # Gestión de amistades
├── gestion_grupos/        # Gestión de grupos
└── recuperar_contrasena/  # Recuperación de contraseña
```

### Principios de Diseño
1. **Separación de Responsabilidades**: Cada ventana tiene una función específica
2. **Reutilización de Componentes**: Paneles modulares que se pueden usar en diferentes contextos
3. **Comunicación Asíncrona**: El cliente se comunica con el servidor sin bloquear la UI
4. **Gestión de Estado**: Cada ventana mantiene su propio estado interno

---

## Estructura de Componentes

### 1. Módulo de Autenticación (`ui.autenticacion`)

#### `ventana_login.java`
**Propósito**: Ventana principal de acceso al sistema.

**Componentes Visuales**:
- `panel_formulario_login`: Panel con campos de usuario y contraseña
- Panel de botones con tres acciones: "entrar", "registrarse", "recuperar contrasena"

**Layout**:
- `BorderLayout` como layout principal
- Formulario en el centro (`BorderLayout.CENTER`)
- Botones en la parte inferior (`BorderLayout.SOUTH`)

**Funcionalidad**:
- Configuración de ventana: 350x300 píxeles, no redimensionable, centrada
- Integración con `manejador_login` para procesar autenticación
- Callback pattern para manejar login exitoso y redirección a ventana principal
- Manejo de máximo de intentos fallidos (3 intentos)

**Flujo de Ejecución**:
1. Usuario ingresa credenciales
2. Al hacer clic en "entrar", se ejecuta `manejador_login.hacerLogin()`
3. El login se ejecuta en un hilo separado para no bloquear la UI
4. Si es exitoso, se llama al callback `onLoginExitoso()` que abre `ventana_principal_chat`
5. Si falla 3 veces, se redirige automáticamente al registro

#### `panel_formulario_login.java`
**Propósito**: Componente reutilizable para capturar credenciales.

**Componentes**:
- `JTextField txtUsername`: Campo de texto para nombre de usuario
- `JPasswordField txtPassword`: Campo de contraseña (oculta caracteres)

**Layout**:
- `GridLayout(3, 2, 5, 10)`: 3 filas, 2 columnas con espaciado

**Métodos Públicos**:
- `getUsername()`: Obtiene el texto del campo usuario (trimmed)
- `getPassword()`: Obtiene la contraseña (trimmed)
- `limpiar()`: Limpia ambos campos

#### `manejador_login.java`
**Propósito**: Lógica de negocio para el proceso de autenticación.

**Características**:
- **Patrón Callback**: Interfaz `LoginCallback` con dos métodos:
  - `onLoginExitoso(Usuario usuario)`: Se ejecuta cuando el login es exitoso
  - `onMaxIntentosAlcanzados()`: Se ejecuta cuando se alcanzan 3 intentos fallidos

**Proceso de Login**:
1. Valida que los campos no estén vacíos
2. Crea un nuevo hilo para no bloquear la UI
3. Conecta al servidor usando `Cliente.getInstance().conectar()`
4. Crea una `Peticion` con acción "LOGIN" y el objeto `Usuario`
5. Envía la petición y espera respuesta
6. Procesa la respuesta en el hilo de Swing usando `SwingUtilities.invokeLater()`

**Manejo de Errores**:
- `ConnectException`: Muestra mensaje de error de conexión
- `SocketTimeoutException`: Muestra mensaje de timeout
- Errores genéricos: Muestra tipo de excepción y mensaje

**Estados de Respuesta**:
- `"LOGIN_OK"`: Login exitoso, ejecuta callback
- `"LOGIN_BLOQUEADO"`: Cuenta bloqueada
- Otros: Incrementa contador de intentos fallidos

#### `manejador_registro.java`
**Propósito**: Maneja el proceso de registro de nuevos usuarios.

**Interfaz**:
- Usa `JOptionPane.showConfirmDialog()` para mostrar un diálogo modal
- Campos: nombre completo, nuevo usuario, contraseña
- Botón adicional: "recuperar cuenta" que abre `ventana_recuperar_contrasena`

**Proceso**:
1. Muestra diálogo con campos de registro
2. Valida que todos los campos estén llenos
3. Conecta al servidor
4. Crea `Usuario` con los datos ingresados
5. Envía petición "REGISTRO"
6. Muestra mensaje de éxito o error según la respuesta

---

### 2. Módulo de Conversación (`ui.conversacion`)

#### `ventana_principal_chat.java`
**Propósito**: Ventana principal del sistema de chat después del login.

**Estructura Visual**:
```
┌─────────────────────────────────────────────────┐
│  Chat Principal - [Nombre Usuario]             │
├──────────┬──────────────────────────────────────┤
│          │                                      │
│  TABS    │    INVITACIONES                     │
│  (250px) │    PENDIENTES                       │
│          │    (350px)                          │
│ Usuarios │                                      │
│ Amigos   │                                      │
│ Grupos   │                                      │
│          │                                      │
│ [Botones]│                                      │
│ Gestión  │                                      │
└──────────┴──────────────────────────────────────┘
```

**Componentes Principales**:
1. **`JTabbedPane`**: Contiene tres pestañas:
   - **Usuarios**: Lista de todos los usuarios del sistema
   - **Amigos**: Lista de amigos del usuario actual
   - **Grupos**: Lista de grupos a los que pertenece

2. **`panel_invitaciones`**: Panel lateral derecho que muestra:
   - Invitaciones de amistad pendientes
   - Invitaciones a grupos pendientes
   - Botones para aceptar/rechazar

3. **Panel de Botones**: 
   - "Gestión Amigos": Abre `ventana_gestion_amigos`
   - "Gestión Grupos": Abre `ventana_gestion_grupos`

**Layout**:
- `JSplitPane` horizontal divide la ventana en dos áreas
- Panel izquierdo: 250px de ancho preferido
- Panel derecho: Invitaciones

**Gestión de Estado**:
- `Map<String, ventana_conversacion> ventanasChatAbiertas`: Controla ventanas de chat abiertas
- `Map<Integer, Usuario> mapaUsuarios`: Cache de usuarios para acceso rápido
- `boolean activo`: Flag para controlar el ciclo de vida

**Eventos Configurados**:
- **ListSelectionListener**: En cada panel (usuarios, amigos, grupos) para detectar selección
- **MouseListener**: Doble clic para abrir chat
- **WindowListener**: Maneja cierre de ventana con confirmación

**Métodos Clave**:
- `abrirChatUsuario()`: Abre chat con usuario seleccionado
- `abrirChatAmigo()`: Abre chat con amigo seleccionado
- `abrirChatGrupo()`: Abre chat de grupo seleccionado
- `solicitarDatosIniciales()`: Envía 5 peticiones al servidor:
  - `OBTENER_USUARIOS`
  - `OBTENER_AMIGOS`
  - `OBTENER_GRUPOS`
  - `OBTENER_INVITACIONES`
  - `OBTENER_INVITACIONES_GRUPO`

**Sistema de Receptores**:
- `receptor_mensajes`: Hilo que escucha mensajes del servidor
- `procesador_peticiones`: Procesa las peticiones recibidas y actualiza la UI

**Cierre de Aplicación**:
- Al cerrar, muestra confirmación
- Detiene el receptor de mensajes
- Cierra todas las ventanas de chat abiertas
- Envía petición "DESCONECTAR" al servidor
- Cierra la conexión del cliente

#### `ventana_conversacion.java`
**Propósito**: Ventana individual para cada conversación (chat 1-a-1 o grupo).

**Dos Constructores**:
1. `ventana_conversacion(Usuario usuarioActual, Usuario destinatario)`: Chat individual
2. `ventana_conversacion(Usuario usuarioActual, int grupoId, String tituloGrupo)`: Chat de grupo

**Estructura Visual**:
```
┌─────────────────────────────────────┐
│ Chat con [Nombre] / Grupo: [Título] │
├─────────────────────────────────────┤
│                                     │
│   [Área de Mensajes]                │
│   (Scrollable)                      │
│                                     │
│                                     │
├─────────────────────────────────────┤
│ [Campo Texto]  [Botón Enviar]      │
└─────────────────────────────────────┘
```

**Componentes**:
- `panel_mensajes`: Área scrollable donde se muestran los mensajes
- `panel_envio`: Panel inferior con campo de texto y botón enviar

**Funcionalidad**:
- Carga historial de mensajes al abrir
- Muestra mensaje de bienvenida
- Envía mensajes al servidor
- Recibe mensajes en tiempo real
- Auto-scroll al final cuando llegan nuevos mensajes

**Gestión de Mensajes**:
- `cargarHistorial()`: Solicita historial al servidor al abrir
- `enviarMensaje()`: Envía mensaje y lo muestra inmediatamente (optimistic update)
- `recibirMensaje()`: Método público para recibir mensajes del procesador

**Identificación de Chat**:
- `esChatCon(int id, boolean esGrupo)`: Verifica si esta ventana corresponde a un chat específico
- Usado por el procesador para enrutar mensajes a la ventana correcta

---

### 3. Componentes Reutilizables (`ui.conversacion.componentes`)

#### `panel_mensajes.java`
**Propósito**: Componente para mostrar mensajes en una conversación.

**Diseño Visual**:
- Estilo similar a WhatsApp/Telegram
- Mensajes propios: Alineados a la derecha, fondo verde claro (`Color(220, 248, 198)`)
- Mensajes recibidos: Alineados a la izquierda, fondo blanco, con avatar circular
- Burbujas de mensaje con bordes redondeados
- Timestamp en cada mensaje (formato HH:mm)

**Estructura de un Mensaje**:
```
┌─────────────────────────────────────┐
│ [Avatar]  ┌──────────────────────┐ │
│           │ Nombre (solo grupos) │ │
│           │ Contenido del mensaje │ │
│           │            HH:mm      │ │
│           └──────────────────────┘ │
└─────────────────────────────────────┘
```

**Características**:
- `BoxLayout` vertical para apilar mensajes
- `JScrollPane` para hacer scrollable el área
- Auto-scroll al final cuando se agregan mensajes
- Soporte para HTML en mensajes (con escape de caracteres especiales)
- Ancho máximo de 350px para burbujas
- Espaciado vertical de 4px entre mensajes

**Métodos**:
- `agregarMensaje(String mensaje, String nombreRemitente, int remitenteId, boolean esGrupo)`: Agrega mensaje normal
- `agregarMensajeSistema(String mensaje)`: Agrega mensaje del sistema (estilo diferente)
- `limpiar()`: Limpia todos los mensajes

**Thread Safety**:
- Todos los métodos de actualización usan `SwingUtilities.invokeLater()` para ejecutarse en el Event Dispatch Thread (EDT)

#### `panel_envio.java`
**Propósito**: Panel para escribir y enviar mensajes.

**Componentes**:
- `JTextField campoMensaje`: Campo de texto para escribir
- `JButton btnEnviar`: Botón para enviar

**Layout**:
- `BorderLayout`: Campo de texto en el centro, botón a la derecha

**Funcionalidad**:
- El campo de texto tiene un `ActionListener` para enviar al presionar Enter
- El botón también tiene el mismo listener
- `getTexto()`: Obtiene el texto (trimmed)
- `limpiar()`: Limpia el campo
- `focus()`: Devuelve el foco al campo de texto después de enviar

#### `panel_usuarios.java`
**Propósito**: Lista de usuarios disponibles en el sistema.

**Componentes**:
- `DefaultListModel<String> modeloUsuarios`: Modelo de datos para la lista
- `JList<String> listaUsuarios`: Componente visual de la lista
- `JScrollPane`: Hace scrollable la lista
- `Map<String, Integer> mapaUsuarios`: Mapea nombres mostrados a IDs de usuario

**Formato de Visualización**:
- `"[Online]"` o `"[Offline]"` + nombre + `" ("` + username + `")"`
- Ejemplo: `"[Online] Juan Pérez (juan123)"`

**Funcionalidad**:
- `actualizarUsuarios(List<Usuario> usuarios)`: Actualiza la lista desde el servidor
- Filtra el usuario actual (no se muestra a sí mismo)
- `getSeleccionId()`: Obtiene el ID del usuario seleccionado
- `tieneSeleccion()`: Verifica si hay un elemento seleccionado

**Eventos**:
- Permite agregar `ListSelectionListener` y `MouseListener` desde fuera

#### `panel_amigos.java`
**Propósito**: Lista de amigos del usuario actual.

**Estructura Similar a `panel_usuarios`**:
- Mismo patrón de diseño
- `DefaultListModel` + `JList` + `JScrollPane`
- `Map` para mapear nombres a IDs

**Diferencias**:
- Solo muestra amigos con `estado == 1` (amistad aceptada)
- Formato: `"[*]"` (online) o `"[ ]"` (offline) + nombre + username
- Recibe dos listas: `List<Amigo>` y `List<Usuario>` para construir la vista

**Lógica de Amistad**:
- Determina qué usuario es el "otro" comparando `fk_usuario1` y `fk_usuario2` con el ID actual

#### `panel_grupos.java`
**Propósito**: Lista de grupos a los que pertenece el usuario.

**Estructura**:
- Similar a los otros paneles de lista
- Formato: `"[Grupo] " + titulo`

**Funcionalidad**:
- `actualizarGrupos(List<Grupo> grupos)`: Actualiza desde el servidor
- Mapea nombres a `pk_grupo` (ID del grupo)

#### `panel_invitaciones.java`
**Propósito**: Panel para gestionar invitaciones pendientes.

**Estructura Visual**:
- `JTabbedPane` con dos pestañas:
  - **Amigos**: Invitaciones de amistad
  - **Grupos**: Invitaciones a grupos

**Cada Pestaña Contiene**:
- `JList` con las invitaciones
- Panel de botones: "Aceptar" y "Rechazar"

**Formato de Invitaciones**:
- Amigos: `"ID - Nombre"`
- Grupos: `"ID - Título (de Invitador)"`

**Métodos**:
- `actualizarInvitacionesAmigos(List<Amigo>)`: Actualiza invitaciones de amistad
- `actualizarInvitacionesGrupos(List<InvitacionGrupo>)`: Actualiza invitaciones de grupo
- `getSeleccionAmigoId()` / `getSeleccionGrupoId()`: Extrae el ID de la selección
- Getters para los botones (configurados desde `ventana_principal_chat`)

---

### 4. Módulo de Gestión de Amigos (`ui.gestion_amigos`)

#### `ventana_gestion_amigos.java`
**Propósito**: Diálogo modal para gestionar amistades.

**Tipo**: `JDialog` (ventana modal que bloquea la interacción con la ventana padre)

**Estructura**:
```
┌─────────────────────────────┐
│ Gestión de Amigos           │
├─────────────────────────────┤
│ Buscar Usuario              │
│ [Campo] [Buscar]            │
├─────────────────────────────┤
│ Mis Amigos                  │
│ [Lista de Amigos]           │
├─────────────────────────────┤
│        [Cerrar]             │
└─────────────────────────────┘
```

**Funcionalidad**:
- **Buscar Usuario**: Permite buscar usuarios por username y enviar solicitud de amistad
- **Lista de Amigos**: Muestra los amigos actuales
- Al abrir, carga automáticamente la lista de amigos

**Proceso de Búsqueda**:
1. Usuario ingresa username
2. Envía petición "BUSCAR_USUARIO"
3. Si encuentra, muestra confirmación para enviar invitación
4. Si acepta, envía petición "ENVIAR_INVITACION_AMIGO"

---

### 5. Módulo de Gestión de Grupos (`ui.gestion_grupos`)

#### `ventana_gestion_grupos.java`
**Propósito**: Diálogo modal para gestionar grupos.

**Estructura**:
```
┌─────────────────────────────────────┐
│ Gestión de Grupos                   │
├─────────────────────────────────────┤
│ Crear Nuevo Grupo                   │
│ [Título] [Crear Grupo]              │
├──────────────┬──────────────────────┤
│ Mis Grupos   │ Usuarios Disponibles │
│ [Lista]      │ [Lista Multi-select] │
│              │                      │
│ [Botones]    │                      │
└──────────────┴──────────────────────┘
```

**Funcionalidades**:
1. **Crear Grupo**:
   - Ingresa título
   - Selecciona al menos 2 usuarios (mínimo 3 personas incluyendo creador)
   - Envía petición "CREAR_GRUPO"

2. **Agregar Miembro**:
   - Selecciona grupo y usuarios
   - Envía petición "AGREGAR_MIEMBRO_GRUPO" para cada usuario

3. **Eliminar Miembro**:
   - Selecciona grupo
   - Ingresa ID del usuario a eliminar
   - Envía petición "ELIMINAR_MIEMBRO_GRUPO"

4. **Salir del Grupo**:
   - Selecciona grupo
   - Confirma acción
   - Envía petición "SALIR_GRUPO"

**Layout**:
- `JSplitPane` horizontal divide grupos y usuarios disponibles
- Lista de usuarios permite selección múltiple

---

### 6. Módulo de Recuperación de Contraseña (`ui.recuperar_contrasena`)

#### `ventana_recuperar_contrasena.java`
**Propósito**: Diálogo para recuperar/resetear contraseña.

**Estructura**:
```
┌─────────────────────────────┐
│ recuperar contrasena        │
├─────────────────────────────┤
│ usuario:        [Campo]     │
│ nueva contrasena: [Campo]   │
│ confirmar:      [Campo]     │
│                             │
│    [recuperar] [cancelar]   │
└─────────────────────────────┘
```

**Layout**:
- `GridBagLayout`: Layout flexible para alinear campos y etiquetas

**Validaciones**:
- Verifica que todos los campos estén llenos
- Verifica que ambas contraseñas coincidan

**Proceso**:
1. Valida campos
2. Ejecuta en hilo separado
3. Conecta al servidor
4. Envía petición "RECUPERAR_CONTRASENA" con username y nueva contraseña
5. Muestra resultado

---

## Sistema de Comunicación Cliente-Servidor

### Arquitectura de Comunicación

La UI se comunica con el servidor a través de la clase `Cliente` (patrón Singleton):

```java
Cliente.getInstance().conectar();
Cliente.getInstance().enviar(peticion);
Peticion respuesta = Cliente.getInstance().recibir();
```

### Objeto `Peticion`

Todas las comunicaciones usan el objeto `Peticion` que contiene:
- `String accion`: Tipo de operación (ej: "LOGIN", "MENSAJE_AMIGO", etc.)
- `Object datos`: Datos asociados a la petición

### Tipos de Peticiones Utilizadas

**Autenticación**:
- `"LOGIN"`: Iniciar sesión
- `"REGISTRO"`: Registrar nuevo usuario
- `"RECUPERAR_CONTRASENA"`: Resetear contraseña

**Datos**:
- `"OBTENER_USUARIOS"`: Lista de usuarios
- `"OBTENER_AMIGOS"`: Lista de amigos
- `"OBTENER_GRUPOS"`: Lista de grupos
- `"OBTENER_INVITACIONES"`: Invitaciones de amistad
- `"OBTENER_INVITACIONES_GRUPO"`: Invitaciones a grupos
- `"OBTENER_HISTORIAL"`: Historial de chat individual
- `"OBTENER_HISTORIAL_GRUPO"`: Historial de chat de grupo

**Mensajería**:
- `"MENSAJE_AMIGO"`: Enviar mensaje a amigo
- `"MENSAJE_GRUPO"`: Enviar mensaje a grupo
- `"MENSAJE_RECIBIDO"` / `"MENSAJE_AMIGO_RECIBIDO"`: Mensaje recibido (respuesta del servidor)
- `"MENSAJE_GRUPO_RECIBIDO"`: Mensaje de grupo recibido

**Gestión**:
- `"BUSCAR_USUARIO"`: Buscar usuario por username
- `"ENVIAR_INVITACION_AMIGO"`: Enviar solicitud de amistad
- `"ACEPTAR_INVITACION"` / `"RECHAZAR_INVITACION"`: Responder invitación de amistad
- `"CREAR_GRUPO"`: Crear nuevo grupo
- `"AGREGAR_MIEMBRO_GRUPO"`: Agregar miembro a grupo
- `"ELIMINAR_MIEMBRO_GRUPO"`: Eliminar miembro de grupo
- `"SALIR_GRUPO"`: Salir de un grupo
- `"ACEPTAR_INVITACION_GRUPO"` / `"RECHAZAR_INVITACION_GRUPO"`: Responder invitación de grupo
- `"DESCONECTAR"`: Desconectar del servidor

---

## Gestión de Hilos y Concurrencia

### Thread Safety en Swing

**Regla Fundamental**: Todas las actualizaciones de componentes Swing deben ejecutarse en el **Event Dispatch Thread (EDT)**.

### Uso de `SwingUtilities.invokeLater()`

Cada vez que se necesita actualizar la UI desde un hilo secundario, se usa:

```java
SwingUtilities.invokeLater(() -> {
    // Código que actualiza componentes Swing
    componente.setText("nuevo texto");
});
```

**Ejemplos en el código**:
- Actualización de listas desde el procesador de peticiones
- Actualización de mensajes recibidos
- Mostrar diálogos de error desde hilos de red

### Hilos de Red

**Operaciones Síncronas**:
- Login, registro, búsqueda: Se ejecutan en hilos separados para no bloquear la UI
- Después de recibir respuesta, se actualiza la UI usando `SwingUtilities.invokeLater()`

**Operaciones Asíncronas**:
- `receptor_mensajes`: Hilo daemon que escucha mensajes del servidor continuamente
- Se detiene cuando la aplicación se cierra o se desconecta

### Clase `receptor_mensajes`

**Propósito**: Hilo que escucha mensajes entrantes del servidor en tiempo real.

**Características**:
- Extiende `Thread`
- `setDaemon(true)`: Se detiene automáticamente cuando la aplicación cierra
- Loop infinito que llama a `Cliente.getInstance().recibir()`
- Procesa peticiones usando `procesador_peticiones`

**Manejo de Errores**:
- `EOFException`: Stream cerrado, detiene el receptor
- `StreamCorruptedException`: Stream corrupto, reintenta hasta 3 veces
- `SocketException`: Conexión cerrada, detiene el receptor
- `ClassCastException`: Error de tipo, reintenta hasta 3 veces
- Después de 3 errores consecutivos, se detiene

**Control de Estado**:
- `boolean activo`: Flag para controlar el loop
- `detener()`: Método para detener el receptor de forma segura

### Clase `procesador_peticiones`

**Propósito**: Procesa las peticiones recibidas y actualiza los componentes de la UI.

**Patrón**: Switch-case que maneja diferentes tipos de acciones

**Acciones Procesadas**:
- `MENSAJE_RECIBIDO` / `MENSAJE_AMIGO_RECIBIDO`: Enruta mensaje a la ventana de chat correcta
- `MENSAJE_GRUPO_RECIBIDO`: Enruta mensaje de grupo
- `USUARIOS_CONECTADOS` / `LISTA_USUARIOS`: Actualiza lista de usuarios
- `USUARIO_CONECTO` / `USUARIO_DESCONECTO`: Solicita lista actualizada
- `AMIGOS_OBTENIDOS`: Actualiza lista de amigos
- `GRUPOS_OBTENIDOS`: Actualiza lista de grupos
- `INVITACIONES_OBTENIDAS`: Actualiza invitaciones de amistad
- `INVITACIONES_GRUPO_OBTENIDAS`: Actualiza invitaciones de grupo
- `MENSAJE_ERROR`: Muestra diálogo de error

**Enrutamiento de Mensajes**:
- Determina qué ventana de chat debe recibir el mensaje
- Usa claves como `"amigo_ID"`, `"usuario_ID"`, `"grupo_ID"`
- Si la ventana no está abierta, el mensaje se pierde (podría mejorarse guardando mensajes pendientes)

---

## Patrones de Diseño Implementados

### 1. Singleton
- `Cliente.getInstance()`: Una única instancia de conexión al servidor

### 2. Callback Pattern
- `manejador_login.LoginCallback`: Interfaz para manejar resultados de login
- Permite desacoplar la lógica de login de la UI

### 3. Observer Pattern (Implícito)
- `ListSelectionListener`: Observa cambios en selección de listas
- `MouseListener`: Observa eventos de mouse
- `WindowListener`: Observa eventos de ventana

### 4. Model-View
- `DefaultListModel` + `JList`: Separación entre modelo de datos y vista
- Los paneles de lista actualizan el modelo, y la vista se actualiza automáticamente

### 5. Factory Pattern (Implícito)
- Constructores de `ventana_conversacion` crean diferentes tipos de chat según parámetros

### 6. Strategy Pattern (Implícito)
- Diferentes layouts según el contexto (BorderLayout, FlowLayout, etc.)

---

## Flujo de Navegación Completo

### Flujo de Inicio de Sesión
```
1. Aplicación inicia → ventana_login se muestra
2. Usuario ingresa credenciales → Click en "entrar"
3. manejador_login.hacerLogin() ejecuta en hilo separado
4. Conecta al servidor
5. Envía petición "LOGIN"
6. Recibe respuesta
7. Si "LOGIN_OK" → callback.onLoginExitoso()
8. Se crea ventana_principal_chat
9. ventana_login se cierra (dispose)
10. ventana_principal_chat solicita datos iniciales
11. Se inicia receptor_mensajes
```

### Flujo de Envío de Mensaje
```
1. Usuario escribe mensaje en panel_envio
2. Click en "Enviar" o presiona Enter
3. ventana_conversacion.enviarMensaje()
4. Muestra mensaje inmediatamente (optimistic update)
5. Limpia campo de texto
6. Crea objeto Mensaje
7. Envía petición "MENSAJE_AMIGO" o "MENSAJE_GRUPO"
8. Servidor procesa y reenvía a destinatario(s)
9. receptor_mensajes recibe "MENSAJE_RECIBIDO"
10. procesador_peticiones enruta a ventana correcta
11. ventana_conversacion.recibirMensaje() actualiza UI
```

### Flujo de Apertura de Chat
```
1. Usuario selecciona elemento en lista (usuarios/amigos/grupos)
2. ListSelectionListener o MouseListener detecta evento
3. ventana_principal_chat verifica si ya existe ventana abierta
4. Si existe → ventana.toFront() (trae al frente)
5. Si no existe → Crea nueva ventana_conversacion
6. Agrega a ventanasChatAbiertas con clave única
7. ventana_conversacion carga historial
8. Muestra mensaje de bienvenida
9. Inicia receptor_mensajes para esta ventana
```

### Flujo de Gestión de Invitaciones
```
1. Usuario selecciona invitación en panel_invitaciones
2. Click en "Aceptar" o "Rechazar"
3. ventana_principal_chat.responderInvitacionAmigo/Grupo()
4. Envía petición "ACEPTAR_INVITACION" o "RECHAZAR_INVITACION"
5. Recibe respuesta
6. Solicita datos actualizados (solicitarDatosIniciales())
7. panel_invitaciones se actualiza automáticamente
```

---

## Consideraciones de Diseño

### Ventajas del Diseño Actual

1. **Modularidad**: Cada componente tiene una responsabilidad clara
2. **Reutilización**: Paneles pueden usarse en diferentes contextos
3. **Separación de Concerns**: Lógica de negocio separada de UI
4. **Thread Safety**: Uso correcto de SwingUtilities.invokeLater()
5. **Experiencia de Usuario**: Actualizaciones optimistas (mensajes se muestran antes de confirmación)

### Áreas de Mejora Potenciales

1. **Mensajes Pendientes**: Si una ventana de chat no está abierta, los mensajes se pierden
2. **Reconexión**: No hay manejo automático de desconexiones
3. **Notificaciones**: No hay sistema de notificaciones para mensajes nuevos
4. **Búsqueda**: No hay búsqueda en historial de mensajes
5. **Multimedia**: Solo soporta texto plano
6. **Temas**: No hay personalización de colores/temas
7. **Validación**: Algunas validaciones podrían ser más robustas

---

## Resumen Técnico

### Stack Tecnológico
- **Lenguaje**: Java
- **UI Framework**: Java Swing
- **Comunicación**: Sockets TCP/IP
- **Serialización**: Java Object Serialization
- **Threading**: Thread nativo de Java + Swing EDT

### Arquitectura
- **Tipo**: Cliente-Servidor
- **Patrón de Comunicación**: Request-Response + Push (mensajes en tiempo real)
- **Modelo de Datos**: POJOs (Plain Old Java Objects)

### Componentes Clave
- **Ventanas**: 6 ventanas principales
- **Paneles Reutilizables**: 6 componentes modulares
- **Hilos**: 1+ hilos de red por ventana principal
- **Manejo de Estado**: Maps y flags booleanos

### Líneas de Código Aproximadas
- Total UI: ~2000+ líneas
- Componentes: ~800 líneas
- Lógica de negocio: ~600 líneas
- Gestión de comunicación: ~400 líneas

---

## Conclusión

La interfaz de usuario de esta aplicación de chat está bien estructurada siguiendo principios de diseño orientado a objetos y mejores prácticas de Swing. Utiliza una arquitectura modular que facilita el mantenimiento y la extensión. El sistema de comunicación asíncrona permite una experiencia de usuario fluida sin bloqueos, y el manejo adecuado de hilos garantiza la estabilidad de la aplicación.

El código demuestra un buen entendimiento de:
- Programación orientada a objetos
- Gestión de hilos y concurrencia
- Patrones de diseño
- Arquitectura cliente-servidor
- Desarrollo de interfaces gráficas con Swing

