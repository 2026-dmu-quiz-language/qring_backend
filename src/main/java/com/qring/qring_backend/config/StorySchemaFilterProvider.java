package com.qring.qring_backend.config;

import com.qring.qring_backend.domain.content.StorySessionEntity;
import org.hibernate.mapping.Table;
import org.hibernate.tool.schema.internal.DefaultSchemaFilter;
import org.hibernate.tool.schema.spi.SchemaFilter;
import org.hibernate.tool.schema.spi.SchemaFilterProvider;

/**
 * 스토리 세션 테이블을 Hibernate 자동 스키마 생성(ddl-auto: update)에서 제외한다.
 * 이 테이블은 DB 담당 팀원이 직접 생성하기로 했으므로, 서버가 기동하면서
 * 대신 만들어버리지 않도록 막는다. 런타임 조회/저장(DML)에는 영향이 없다.
 *
 * 테이블명은 StorySessionEntity.TABLE_NAME 을 참조하므로,
 * DB 담당자가 이름을 확정해 상수를 바꾸면 이 필터도 자동으로 따라간다.
 *
 * application.yml 의 hibernate.hbm2ddl.schema_filter_provider 로 등록된다.
 * 팀원이 테이블을 만든 뒤에도 이 필터는 그대로 두면 된다 (update 가 컬럼을 임의 변경하는 것도 막아준다).
 */
public class StorySchemaFilterProvider implements SchemaFilterProvider {

    private static final SchemaFilter EXCLUDE_STORY_SESSION = new DefaultSchemaFilter() {
        @Override
        public boolean includeTable(Table table) {
            return !StorySessionEntity.TABLE_NAME.equalsIgnoreCase(table.getName());
        }
    };

    @Override
    public SchemaFilter getCreateFilter() {
        return EXCLUDE_STORY_SESSION;
    }

    @Override
    public SchemaFilter getDropFilter() {
        return EXCLUDE_STORY_SESSION;
    }

    @Override
    public SchemaFilter getMigrateFilter() {
        return EXCLUDE_STORY_SESSION;
    }

    @Override
    public SchemaFilter getValidateFilter() {
        return EXCLUDE_STORY_SESSION;
    }

    @Override
    public SchemaFilter getTruncatorFilter() {
        return EXCLUDE_STORY_SESSION;
    }
}
