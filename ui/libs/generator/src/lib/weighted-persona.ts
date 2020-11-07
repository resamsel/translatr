export interface WeightedPersona {
  section: 'user' | 'project' | 'member' | 'locale' | 'key' | 'translation' | 'access-token';
  type: 'create' | 'read' | 'update' | 'delete';
  name: string;
  description: string;
  weight: number;
}
