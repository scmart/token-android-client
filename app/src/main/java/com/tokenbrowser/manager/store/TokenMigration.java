package com.tokenbrowser.manager.store;


import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

public class TokenMigration implements RealmMigration {
    @Override
    public void migrate(final DynamicRealm realm, final long oldVersion, final long newVersion) {

        final RealmSchema schema = realm.getSchema();
        long currentVersion = oldVersion;

        // Migrate to version 1: Add a new field on User.
        if (currentVersion == 0) {
            schema.get("User")
                    .addField("cacheTimestamp", long.class);
            currentVersion++;
        }

        // Migrate to version 2: Change owner_address to token_id
        if (currentVersion == 1) {
            schema.get("User")
                    .renameField("owner_address", "token_id");
            currentVersion++;
        }
    }
}
