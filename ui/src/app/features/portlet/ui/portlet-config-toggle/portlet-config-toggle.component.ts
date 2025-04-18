// portlet-config-toggle.component.ts
import { Component, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { PortletConfigApiService } from 'features/portlet/api';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpParams } from '@angular/common/http';

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

    toggleConfigForm = this.fb.group({
        userId: ['', [Validators.required, Validators.min(1)]],
        host: ['', [Validators.required]],
        configName: ['', [Validators.required]],
    });

    constructor(private fb: FormBuilder) {}

    // Метод для включения конфигурации
    enableConfig() {
        const { userId, host, configName } = this.toggleConfigForm.value;

        const queryParams = new HttpParams()
            .set('userId', userId)
            .set('host', host)
            .set('configName', configName);

        this.apiService.enableInserterConfig(queryParams)
            .then((data) => {
                console.log('Конфиг включён:', data);
            })
            .catch((err) => {
                console.error('Ошибка при включении конфига:', err);
            });
    }

    // Метод для выключения конфигурации
    disableConfig() {
        const { userId, host, configName } = this.toggleConfigForm.value;

        const queryParams = new HttpParams()
            .set('userId', userId)
            .set('host', host)
            .set('configName', configName);

        this.apiService.disableInserterConfig(queryParams)
            .then((data) => {
                console.log('Конфиг выключен:', data);
            })
            .catch((err) => {
                console.error('Ошибка при выключении конфига:', err);
            });
    }
}
