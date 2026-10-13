// Looking for a language to translate AnythingLLM to?
// RULES:

// please do not submit PR's without first verifying this test passes as it will tell you about missing keys or values

import English from "./en/common.js";
import Korean from "./ko/common.js";
import Spanish from "./es/common.js";
import French from "./fr/common.js";
import Mandarin from "./zh/common.js";
import German from "./de/common.js";
import Estonian from "./et/common.js";
import Russian from "./ru/common.js";
import Italian from "./it/common.js";
import Portuguese from "./pt_BR/common.js";
import Hebrew from "./he/common.js";
import Dutch from "./nl/common.js";
import Vietnamese from "./vn/common.js";
import TraditionalChinese from "./zh_TW/common.js";
import Farsi from "./fa/common.js";
import Turkish from "./tr/common.js";
import Arabic from "./ar/common.js";
import Danish from "./da/common.js";
import Japanese from "./ja/common.js";
import Lativian from "./lv/common.js";
import Polish from "./pl/common.js";
import Romanian from "./ro/common.js";
import Czech from "./cs/common.js";

export const defaultNS = "common";
export const resources = {
  en: {
    common: English,
  },
  zh: {
    common: Mandarin,
  },
  "zh-tw": {
    common: TraditionalChinese,
  },
  es: {
    common: Spanish,
  },
  de: {
    common: German,
  },
  fr: {
    common: French,
  },
  ko: {
    common: Korean,
  },
  et: {
    common: Estonian,
  },
  ru: {
    common: Russian,
  },
  it: {
    common: Italian,
  },
  pt: {
    common: Portuguese,
  },
  he: {
    common: Hebrew,
  },
  nl: {
    common: Dutch,
  },
  vi: {
    common: Vietnamese,
  },
  fa: {
    common: Farsi,
  },
  tr: {
    common: Turkish,
  },
  ar: {
    common: Arabic,
  },
  da: {
    common: Danish,
  },
  ja: {
    common: Japanese,
  },
  lv: {
    common: Lativian,
  },
  pl: {
    common: Polish,
  },
  ro: {
    common: Romanian,
  },
  cs: {
    common: Czech,
  },
};
