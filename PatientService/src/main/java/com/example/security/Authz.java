package com.example.security;
/**
 * 临时鉴权占位：email 分支暂不做鉴权，但现有 Controller 已引用 Authz.assertPatient(...)。
 *
 * 为保证项目能编译/运行，这里提供一个空实现。
 * 后续你要全接口鉴权时，可以在这里接入 JWT/Session，并做 patientId 的归属校验。
 */
public final class Authz {
    private Authz() {
    }
    public static void assertPatient(String patientId) {
        // no-op for now
    }
}