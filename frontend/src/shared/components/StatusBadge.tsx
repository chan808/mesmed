interface Props {
  value: string;
  suffix?: string;
}

export function StatusBadge({ value, suffix }: Props) {
  const cls = suffix ? `badge badge-${value}-${suffix}` : `badge badge-${value}`;
  return <span className={cls}>{value}</span>;
}
