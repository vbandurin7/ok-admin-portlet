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

    availablePlatforms = ['WEB', 'MOB', 'ANDROID', 'API'];

    createConfigForm = this.fb.group({
        host: ['', Validators.required],
        configName: ['', Validators.required],
        positions: ['', Validators.required],
        portletTypes: ['', Validators.required],
        platformTypes: [<string[]>[], Validators.required],
        enableConfig: [false],
        userId: ['']
    });

    onPlatformChange(event: Event) {
        const checkbox = event.target as HTMLInputElement;
        const value = checkbox.value;
        const platformTypes = this.createConfigForm.value.platformTypes;

        if (checkbox.checked && !platformTypes.includes(value)) {
            platformTypes.push(value);
        } else if (!checkbox.checked) {
            const index = platformTypes.indexOf(value);
            if (index > -1) platformTypes.splice(index, 1);
        }

        this.createConfigForm.patchValue({ platformTypes });
    }

    onEnableConfigToggle() {
        const enable = this.createConfigForm.value.enableConfig;
        if (!enable) {
            this.createConfigForm.patchValue({ userId: '' });
        }
    }

    message = '';
    success = false;

    constructor(private fb: FormBuilder) {}

    createConfig() {
        const {
            host,
            configName,
            positions,
            portletTypes,
            platformTypes,
            enableConfig,
            userId
        } = this.createConfigForm.value;

        const positionList = positions.split(',').map(Number);
        const portletTypeList = portletTypes.split(',');

        const portletConfigRequest: any = {
            portlets: [],
            platformTypes,
            enableConfig
        };

        if (enableConfig && userId) {
            portletConfigRequest.userId = userId;
        }

        positionList.forEach((position, index) => {
            const portletType = portletTypeList[index] || '';
            portletConfigRequest.portlets.push({ position, portletType });
        });

        const queryParams = getUrlSearchParams({ host, configName });

        this.apiService.createInserterConfig(portletConfigRequest, queryParams)
            .then((data: any) => {
                const name = data.configName || configName;
                const enabledNote = enableConfig ? ' и включён' : '';
                this.message = `Конфиг "${name}" успешно создан${enabledNote} для платформ: ${platformTypes.join(', ')}.`;
                this.success = true;
            })
            .catch((err) => {
                console.error('Ошибка при создании конфига:', err);
                this.message = 'Ошибка при создании конфига.';
                this.success = false;
            });
    }
}
