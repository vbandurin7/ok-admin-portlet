import { inject, Injectable } from '@angular/core';
import { getUrlSearchParams, Http2 } from 'shared/services/http2';
import { HttpParams } from '@angular/common/http';
import { PortletConfigRequest } from 'features/portlet/models/portlet-config.model';

@Injectable()
export class PortletConfigApiService {
    private readonly http2 = inject(Http2);

    getResolverConfigs(userId: number) {
      return this.http2.getJson(`/api/portlet/resolver/configs/${userId}`);
    }

    createInserterConfig(portletConfigRequest: any, queryParams?: HttpParams) {
        return this.http2.postJson('/api/portlet/createInserterConfig', portletConfigRequest, queryParams);
    }

    // Метод для включения конфигурации
    enableInserterConfig(queryParams: HttpParams) {
        return this.http2.postJson('/api/portlet/enableInserterConfig', {}, queryParams);
    }

    // Метод для выключения конфигурации
    disableInserterConfig(queryParams: HttpParams) {
        return this.http2.postJson('/api/portlet/disableInserterConfig', {}, queryParams);
    }
}
