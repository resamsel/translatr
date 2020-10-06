import { WeightedPersonaFactory } from './weighted-persona-factory';

export const selectPersonaFactory = (
  personas: WeightedPersonaFactory[],
  totalWeight: number
): WeightedPersonaFactory => {
  const threshold = Math.floor(Math.random() * totalWeight);

  let total = 0;
  for (const persona of personas) {
    total += persona.weight;

    if (total >= threshold) {
      return persona;
    }
  }

  return personas[0];
};
