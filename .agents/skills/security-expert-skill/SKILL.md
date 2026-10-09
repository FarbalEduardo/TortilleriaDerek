---
name: security-expert-skill
description: Auditoría de Criptografía, Storage Access Framework, PBKDF2 local y reglas de backup Android para Tortillería Derek.
---

# 🛡️ Skill: Security Expert & Cryptographic Protection

Este skill define los estándares de seguridad offline, criptografía local y protección de bases de datos para el punto de venta **Tortillería Derek**.

---

## 🔒 1. Principios de Seguridad Offline-First

1. **Cero Superficie de Ataque de Red:**
   - La aplicación no debe incluir el permiso `INTERNET`.
   - Prohibido cualquier componente que transmita datos a la red sin interacción y consentimiento explícito del usuario.

2. **Criptografía de Contraseñas y PINs:**
   - Uso obligatorio de `PBKDF2WithHmacSHA256` con un mínimo de **10,000 iteraciones** y salt aleatorio criptográfico de **16 bytes** generado mediante `SecureRandom()`.
   - Las contraseñas en texto plano jamás deben persistirse en SQLite ni viajar en logs.

3. **Transferencia de Base de Datos Segura:**
   - Todo volcado o exportación de SQLite debe forzar previamente un checkpoint WAL atómico: `PRAGMA wal_checkpoint(FULL)`.
   - El archivo exportado debe acompañarse de un hash de integridad **SHA-256** para detectar corrupción flash o manipulación externa.
   - En la importación, debe mantenerse siempre una copia de rescate `.prev` para posibilitar rollback automático si la base de datos entrante resulta corrupta.

4. **Blindaje de Respaldo del Sistema Operativo:**
   - [backup_rules.xml](file:///d:/TortilleriaDerek/app/src/main/res/xml/backup_rules.xml) y [data_extraction_rules.xml](file:///d:/TortilleriaDerek/app/src/main/res/xml/data_extraction_rules.xml) deben excluir explícitamente `tortilleria_db` del Google Cloud Backup automático.

---

## 🔑 2. Mitigación de Credenciales por Defecto

- **Regla Estricta:** La contraseña inicial de fábrica (`admin123`) debe considerarse un vector temporal.
- Al primer inicio o en el primer corte de turno, el sistema debe solicitar al dueño del negocio cambiar la clave maestra para evitar accesos indebidos.
