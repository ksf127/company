{{- define "company-service.name" -}}
{{- .Release.Name -}}
{{- end -}}

{{- define "company-service.labels" -}}
app: {{ include "company-service.name" . }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}
