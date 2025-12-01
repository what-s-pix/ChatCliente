# Estructura del GUI - ChatCliente-Integrado

## Organizacion de Packages UI

```
src/ui/
├── login/                    # Modulo de autenticacion
│   ├── LoginUI.java          # Ventana principal de login
│   ├── LoginFormPanel.java  # Panel del formulario (usuario/contrasena)
│   ├── LoginHandler.java    # Logica de login (intentos, validacion)
│   └── RegistroHandler.java # Logica de registro
│
├── recuperar/                # Modulo de recuperacion
│   └── RecuperarContrasenaUI.java  # Ventana de recuperar contrasena
│
├── chat/                     # Modulo principal del chat
│   ├── ChatUI.java          # Ventana principal del chat
│   ├── ReceptorMensajes.java    # Hilo para recibir mensajes
│   ├── ProcesadorPeticiones.java # Procesa respuestas del servidor
│   └── components/          # Componentes reutilizables del chat
│       ├── MensajesPanel.java    # Panel de area de mensajes
│       ├── UsuariosPanel.java     # Panel de usuarios conectados
│       ├── AmigosPanel.java       # Panel de lista de amigos
│       ├── GruposPanel.java        # Panel de lista de grupos
│       └── EnvioPanel.java        # Panel de envio de mensajes
│
├── amigos/                   # Modulo de gestion de amigos
│   └── GestionAmigosUI.java # Ventana de gestion (buscar, invitar, aceptar)
│
└── grupos/                   # Modulo de gestion de grupos
    └── GestionGruposUI.java  # Ventana de gestion (crear, invitar, administrar)
```

## Estructura Visual del ChatUI

```
┌─────────────────────────────────────────────────────────────┐
│ ChatUI - Ventana Principal                                  │
├──────────────┬──────────────────────────────────────────────┤
│              │                                               │
│  TABBED      │          MENSAJES PANEL                      │
│  PANE        │          (Area de mensajes)                  │
│              │                                               │
│  [conectados]│                                               │
│  [amigos]    │                                               │
│  [grupos]    │                                               │
│              │                                               │
│  ─────────── │                                               │
│              │                                               │
│  [gestion    │                                               │
│   amigos]    │                                               │
│  [gestion    │                                               │
│   grupos]    │                                               │
│              │                                               │
├──────────────┴──────────────────────────────────────────────┤
│                    ENVIO PANEL                              │
│              [campo texto] [enviar]                         │
└─────────────────────────────────────────────────────────────┘
```

## Flujo de Navegacion

1. **LoginUI** → Usuario ingresa credenciales
   - Boton "entrar" → Login normal
   - Boton "registrarse" → Ventana de registro
   - Boton "recuperar contrasena" → Ventana de recuperacion
   - Boton "entrar directamente al chat" → Modo prueba (sin servidor)

2. **ChatUI** → Ventana principal del chat
   - Tab "conectados" → Lista de usuarios online/offline
   - Tab "amigos" → Lista de amigos (con historial)
   - Tab "grupos" → Lista de grupos
   - Boton "gestion amigos" → Abre GestionAmigosUI
   - Boton "gestion grupos" → Abre GestionGruposUI

3. **GestionAmigosUI** → Gestion de relaciones
   - Buscar usuarios
   - Enviar invitaciones
   - Aceptar/rechazar invitaciones
   - Ver lista de amigos

4. **GestionGruposUI** → Gestion de grupos
   - Crear grupos (minimo 3 personas)
   - Invitar miembros
   - Administrar miembros (solo creador)
   - Salir de grupos

## Caracteristicas Implementadas

### Login
- ✅ Contador de intentos (maximo 3)
- ✅ Redireccion automatica a registro tras 3 errores
- ✅ Recuperar contrasena

### Registro
- ✅ Formulario completo
- ✅ Recuperar cuenta

### Chat
- ✅ Lista de conectados/desconectados
- ✅ Lista de amigos
- ✅ Lista de grupos
- ✅ Historial (solo para amigos)
- ✅ Mensajes pendientes para grupos

### Amigos
- ✅ Buscar usuarios
- ✅ Invitaciones
- ✅ Aceptar/rechazar

### Grupos
- ✅ Crear grupos (min 3 personas)
- ✅ Invitaciones
- ✅ Gestion de miembros
- ✅ Validacion de grupos

## Archivos por Modulo

### Login (4 archivos)
- LoginUI.java (~75 lineas)
- LoginFormPanel.java (~35 lineas)
- LoginHandler.java (~75 lineas)
- RegistroHandler.java (~65 lineas)

### Recuperar (1 archivo)
- RecuperarContrasenaUI.java (~110 lineas)

### Chat (8 archivos)
- ChatUI.java (~275 lineas)
- ReceptorMensajes.java (~40 lineas)
- ProcesadorPeticiones.java (~85 lineas)
- components/MensajesPanel.java (~45 lineas)
- components/UsuariosPanel.java (~70 lineas)
- components/AmigosPanel.java (~75 lineas)
- components/GruposPanel.java (~65 lineas)
- components/EnvioPanel.java (~35 lineas)

### Amigos (1 archivo)
- GestionAmigosUI.java (~200 lineas)

### Grupos (1 archivo)
- GestionGruposUI.java (~350 lineas)

**Total: 15 archivos UI organizados en 5 modulos independientes**

