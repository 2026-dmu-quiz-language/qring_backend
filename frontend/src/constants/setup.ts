import type { Language, LevelCode } from '../api/auth';

export interface LanguageOption {
  value: Language;
  label: string;
}

export interface LevelOption {
  value: LevelCode;
  label: string;
  sub: string;
}

export const LANGUAGE_OPTIONS: LanguageOption[] = [
  { value: 'JA', label: '일본어' },
  { value: 'EN', label: '영어' },
  { value: 'ZH', label: '중국어' },
];

export const LEVEL_OPTIONS: LevelOption[] = [
  { value: 1, label: '왕초보', sub: 'Lv.1' },
  { value: 2, label: '일상 회화', sub: 'Lv.2' },
  { value: 3, label: '프리토킹', sub: 'Lv.3' },
];
