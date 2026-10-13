import { AVAILABLE_LLM_PROVIDERS } from "@/pages/GeneralSettings/LLMPreference";
import { DISABLED_PROVIDERS } from "@/hooks/useGetProvidersModels";

export function autoScrollToSelectedLLMProvider(
  selectedLLMProvider,
  timeout = 500
) {
  setTimeout(() => {
    const selectedButton = document.querySelector(
      `[data-llm-value="${selectedLLMProvider}"]`
    );
    if (!selectedButton) return;
    selectedButton.scrollIntoView({ behavior: "smooth", block: "nearest" });
  }, timeout);
}


export function validatedModelSelection(model) {
  try {
    // If the entire select element is not found, return the model as is and cross our fingers
    const selectOption = document.getElementById(`workspace-llm-model-select`);
    if (!selectOption) return model;

    const selectedOption = selectOption.querySelector(
      `option[value="${model}"]`
    );
    if (!selectedOption) return selectOption.querySelector(`option`).value;

    // If the model is in the dropdown, return the model as is
    return model;
  } catch (error) {
    return null; // If the dropdown was empty or something else went wrong, return null to abort the save
  }
}

export function hasMissingCredentials(settings, provider) {
  const providerEntry = AVAILABLE_LLM_PROVIDERS.find(
    (p) => p.value === provider
  );
  if (!providerEntry) return false;

  for (const requiredKey of providerEntry.requiredConfig) {
    if (!settings.hasOwnProperty(requiredKey)) return true;
    if (!settings[requiredKey]) return true;
  }
  return false;
}

export const WORKSPACE_LLM_PROVIDERS = AVAILABLE_LLM_PROVIDERS.filter(
  (provider) => !DISABLED_PROVIDERS.includes(provider.value)
);
