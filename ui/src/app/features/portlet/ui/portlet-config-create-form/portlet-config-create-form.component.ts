import { Component, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { PortletConfigApiService } from 'features/portlet/api';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { getUrlSearchParams, Http2 } from 'shared/services/http2';

@Component({
    standalone: true,
    selector: 'app-portlet-config-create-form',
    templateUrl: './portlet-config-create-form.component.html',
    styleUrls: ['./portlet-config-create-form.component.scss'],
    providers: [PortletConfigApiService],
    imports: [CommonModule, ReactiveFormsModule]
})
export class PortletConfigCreateFormComponent {
    readonly apiService = inject(PortletConfigApiService);

    createConfigForm = this.fb.group({
        host: ['', [Validators.required]],
        configName: ['', [Validators.required]],
        positions: ['', [Validators.required]],
        portletTypes: ['', [Validators.required]]
    });

    constructor(private fb: FormBuilder) {}

    createConfig() {
        const { host, configName, positions, portletTypes } = this.createConfigForm.value;

        const positionList = positions.split(',').map(Number);
        const portletTypeList = portletTypes.split(',');

        const portletConfigRequest = { portlets: [] };

        positionList.forEach((position, index) => {
            const portletType = portletTypeList[index] || '';
            portletConfigRequest.portlets.push({ position, portletType });
        });

        const queryParams = getUrlSearchParams( { host, configName });

        this.apiService.createInserterConfig(portletConfigRequest, queryParams)
            .then((data) => {
                console.log('Конфиг успешно создан:', data);
            })
            .catch((err) => {
                console.error('Ошибка при создании конфига:', err);
            });
    }
}
