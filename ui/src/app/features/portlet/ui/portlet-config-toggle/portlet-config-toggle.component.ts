// portlet-config-toggle.component.ts
import { Component, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { PortletConfigApiService } from 'features/portlet/api';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpParams } from '@angular/common/http';
import { EnableConfigResponse } from 'features/portlet/models';

@Component({
    standalone: true,
    selector: 'app-portlet-config-toggle',
    templateUrl: './portlet-config-toggle.component.html',
    styleUrls: ['./portlet-config-toggle.component.scss'],
    providers: [PortletConfigApiService],
    imports: [CommonModule, ReactiveFormsModule]
})
export class PortletConfigToggleComponent {
    readonly apiService = inject(PortletConfigApiService);
    resultMessage: string | null = null; // ← переменная для отображения результата

    toggleConfigForm = this.fb.group({
        userId: ['', [Validators.required, Validators.min(1)]],
        host: ['', [Validators.required]],
        configName: ['', [Validators.required]],
    });

    constructor(private fb: FormBuilder) {}

    enableConfig() {
        const { userId, host, configName } = this.toggleConfigForm.value;

        const queryParams = new HttpParams()
            .set('userId', userId)
            .set('host', host)
            .set('configName', configName);

        this.apiService.enableInserterConfig(queryParams)
            .then((response: EnableConfigResponse) => {
                const enabledName = response.configName || configName;
                this.resultMessage = `Конфигурация успешно включена под именем "${enabledName}".`;
            })
            .catch((err) => {
                this.resultMessage = `Ошибка при включении: ${err.message || err}`;
            });
    }

    disableConfig() {
        const { userId, host, configName } = this.toggleConfigForm.value;

        const queryParams = new HttpParams()
            .set('userId', userId)
            .set('host', host)
            .set('configName', configName);

        this.apiService.disableInserterConfig(queryParams)
            .then(() => {
                this.resultMessage = `Конфигурация "${configName}" успешно выключена.`;
            })
            .catch((err) => {
                this.resultMessage = `Ошибка при выключении: ${err.message || err}`;
            });
    }
}
