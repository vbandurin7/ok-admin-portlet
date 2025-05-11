import { inject, Injectable } from '@angular/core';
import { getUrlSearchParams, Http2 } from 'shared/services/http2';
import { HttpParams } from '@angular/common/http';
import { PortletConfigRequest, PortletStatsResponse, EnableConfigResponse } from 'features/portlet/models/portlet-config.model';

@Injectable()
export class PortletConfigApiService {
    private readonly http2 = inject(Http2);

    getResolverConfigs(userId: number) {
//       const query = getUrlSearchParams({ userId });
      return this.http2.getJson(`/api/portlet/resolver/configs/${userId}`);
    }

    createInserterConfig(portletConfigRequest: any, queryParams?: HttpParams) {
        return this.http2.postJson('/api/portlet/createInserterConfig', portletConfigRequest, queryParams);
    }

    enableInserterConfig(queryParams: HttpParams) {
        return this.http2.postJson<EnableConfigResponse>('/api/portlet/enableInserterConfig', {}, queryParams);
    }

    disableInserterConfig(queryParams: HttpParams) {
        return this.http2.postJson<{ result: string }>('/api/portlet/disableInserterConfig', {}, queryParams);
    }

    getPortletStats(type: string) {
        return this.http2.getJson<PortletStatsResponse>(`/api/portlet/stats?type=${encodeURIComponent(type)}`);
    }
}
